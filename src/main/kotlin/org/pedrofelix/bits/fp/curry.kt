package org.pedrofelix.bits.fp

fun <P0, P1, R> ((P0, P1) -> R).curry() = { p0: P0 -> { p1: P1 -> this(p0, p1) } }
fun <P0, P1, P2, R> ((P0, P1, P2) -> R).curry() = { p0: P0 -> { p1: P1 -> { p2: P2 -> this(p0, p1, p2) } } }
fun <P0, P1, P2, P3, R> ((P0, P1, P2, P3) -> R).curry() =
    { p0: P0 -> { p1: P1 -> { p2: P2 -> { p3: P3 -> this(p0, p1, p2, p3) } } } }
fun <P0, P1, P2, P3, P4, R> ((P0, P1, P2, P3, P4) -> R).curry() =
    { p0: P0 -> { p1: P1 -> { p2: P2 -> { p3: P3 -> { p4: P4 -> this(p0, p1, p2, p3, p4) } } } } }
