package org.pedrofelix.bits.validation

import org.pedrofelix.bits.fp.curry

// based on https://fsharpforfunandprofit.com/posts/elevated-world-3/#validation

// The applicative structure - a result type that represents both
// - validation success, containing an `A` value
// - validation failure, containing a list of error strings
sealed class Result<A>
class Success<A>(val value: A) : Result<A>()
class Failure<A>(val errors: List<String>) : Result<A>()

// The functors's `map` function
fun <A, B> map(f: (A) -> B, result: Result<A>): Result<B> = when (result) {
    is Success<A> -> Success(f(result.value))
    is Failure<A> -> Failure(result.errors)
}
operator fun <A,B> ((A) -> B).plus(result: Result<A>) = map(this, result)

// The applicative's `return` (aka `pure`) function
fun <A> retn(a: A) = Success(a)

// The applicative's `apply` (aka `<*>`` function
fun <A, B> apply(f: Result<(A) -> B>, result: Result<A>): Result<B> = when (f) {
    is Success<(A) -> B> -> when (result) {
        is Success<A> -> Success(f.value(result.value))
        is Failure<A> -> Failure(result.errors)
    }
    is Failure<(A) -> B> -> when (result) {
        is Success<A> -> Failure(f.errors)
        is Failure<A> -> Failure(f.errors + result.errors)
    }
}
operator fun <A,B> (Result<(A) -> B>).times(result: Result<A>) = apply(this, result)
