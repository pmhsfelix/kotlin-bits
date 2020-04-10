package org.pedrofelix.bits.fp

sealed class Maybe<out T>
data class Just<T>(val value: T) : Maybe<T>()
object None : Maybe<Nothing>()
