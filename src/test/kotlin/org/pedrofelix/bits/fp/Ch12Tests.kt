package org.pedrofelix.bits.fp

import org.junit.Assert.assertEquals
import org.junit.Test
import org.pedrofelix.bits.fp.*

class Ch12Tests {

    @Test
    fun maybe_based_tests() {
        val evals: List<(Expr)->Maybe<Int>> = listOf(
            ::eval00, ::eval01
        )
        for(eval in evals) {
            assertEquals(Just(2), eval(Div(Val(4), Val(2))))
            assertEquals(None, eval(Div(Val(4), Val(0))))
            assertEquals(None, eval(Div(Div(Val(4), Val(0)), Val(1))))
        }
    }

    @Test
    fun nullable_based_bests() {
        val evals: List<(Expr)->Int?> = listOf(
            ::eval10, ::eval11
        )
        for(eval in evals) {
            assertEquals(2, eval(Div(Val(4), Val(2))))
            assertEquals(null, eval(Div(Val(4), Val(0))))
            assertEquals(null, eval(Div(Div(Val(4), Val(0)), Val(1))))
        }
    }
}