/*
 * A rather simple, and mostly useless, cooperative threading kernel,
 * using suspend functions.
 */

package org.pedrofelix.bits

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slf4j.LoggerFactory
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.*
import kotlin.coroutines.resume

private val log = LoggerFactory.getLogger("cthreads")

/*
 * Queue type and operations.
 */
typealias Queue<T> = MutableList<T>

fun <T> Queue<T>.enqueue(t: T) = this.add(t)
fun <T> Queue<T>.dequeue() = this.removeAt(0)

/*
 * A non-running thread is represented by a continuation.
 * Resuming the thread execution is done by calling that continuation.
 */
typealias ThreadContinuation = Continuation<Unit>

/*
 * The list of ready threads is just the list of ready continuations.
 */
private val readyList = mutableListOf<ThreadContinuation>()

/*
 * The scheduling function.
 * All the cooperative threads run in the context of the JVM thread where this function is called.
 */
fun schedule() {
    while (true) {
        if (readyList.isEmpty()) {
            // Since we don't have any I/O or timers, there isn't anything left to do if the ready list is empty.
            log.info("Ready list is empty, nothing else to do. Ending.")
            return
        }
        /*
         * Scheduling: just take the first continuation of the list and run it.
         * This call will return when the cooperative thread suspends or ends.
         */
        readyList.dequeue().resume(Unit)
    }
}

/*
 * Suspend current cooperative thread, but keep it ready.
 */
suspend fun yield() =
    suspendCoroutineUninterceptedOrReturn<Unit> { cont ->
        readyList.enqueue(cont)
        COROUTINE_SUSPENDED
    }

/*
 * Suspend current cooperative thread, storing it in the given wait queue.
 */
suspend fun park(waitQueue: Queue<ThreadContinuation>) =
    suspendCoroutineUninterceptedOrReturn<Unit> { cont ->
        waitQueue.enqueue(cont)
        COROUTINE_SUSPENDED
    }

/*
 * Continuation that represents the start of a cooperative thread.
 */
private class StartContinuation(private val threadBlock: suspend () -> Unit) : Continuation<Unit> {
    override val context: CoroutineContext get() = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>) {
        threadBlock.startCoroutineUninterceptedOrReturn(EndContinuation())
    }
}

/*
 * Continuation that represents the end of a cooperative thread.
 */
private class EndContinuation() : Continuation<Unit> {
    override val context: CoroutineContext get() = EmptyCoroutineContext
    override fun resumeWith(result: Result<Unit>) {
        log.info("cooperative thread ending")
    }
}

/*
 * Create and start a cooperative thread.
 * A cooperative thread is represented by a suspend function without parameters and returning Unit.
 */
fun createAndStartCooperativeThread(threadBlock: suspend () -> Unit) {
    // Here we just create the continuation to start the thread and store it on the ready list.
    readyList.add(StartContinuation(threadBlock))
}

/*
 * Just a simple unary semaphore, without timeouts or cancellation
 */
class Semaphore(private var units: Int) {
    private val waitList = mutableListOf<Continuation<Unit>>()

    suspend fun acquire() = if (units > 0) {
        units -= 1
    } else {
        park(waitList)
    }

    fun release() {
        if (waitList.size > 0) {
            readyList.enqueue(waitList.dequeue())
        } else {
            units += 1
        }
    }
}

// Tests below
class CThreadsTest {

    @Test
    fun test0() {
        val nOfThreads = 1000
        val nOfReps = 1000
        var acc = 0
        val semaphore = Semaphore(1)

        createAndStartCooperativeThread {
            for (i in 1..nOfThreads) {
                createAndStartCooperativeThread {
                    for (j in 1..nOfReps) {
                        semaphore.acquire()
                        acc += 1
                        yield()
                        semaphore.release()
                        yield()
                    }
                }
            }
        }

        schedule()

        assertEquals(nOfThreads * nOfReps, acc)
    }
}