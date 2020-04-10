package org.pedrofelix.bits.fp

/*
 * Examples from chapter 12 of "Programming in Haskell" by Graham Hutton,
 * adapted to Kotlin
 */

// A data type to represent expression
sealed class Expr

data class Val(val value: Int) : Expr()
data class Div(val left: Expr, val right: Expr) : Expr()

// safediv using the `Maybe` type
fun safediv0(x: Int, y: Int): Maybe<Int> = if (y == 0) None else Just(x / y)

// safediv using nullable types
fun safediv1(x: Int, y: Int): Int? = if (y == 0) null else x / y

// recursion-based eval:
// - using Mayble
fun eval00(expr: Expr): Maybe<Int> =
    when (expr) {
        is Val -> Just(expr.value)
        is Div -> when (val left = eval00(expr.left)) {
            is None -> None
            is Just -> when (val right = eval00(expr.right)) {
                is None -> None
                is Just -> safediv0(left.value, right.value)
            }
        }
    }

// - using nullable types
fun eval10(expr: Expr): Int? =
    when (expr) {
        is Val -> expr.value
        is Div -> when (val left = eval10(expr.left)) {
            null -> null
            else -> when (val right = eval10(expr.right)) {
                null -> null
                else -> safediv1(left, right)
            }
        }
    }

// flatMap-based eval:
fun <T> Maybe<T>.flatMap(f: (T) -> Maybe<T>) = when (this) {
    is None -> None
    is Just -> f(this.value)
}

// - using Mayble
fun eval01(expr: Expr): Maybe<Int> =
    when (expr) {
        is Val -> Just(expr.value)
        is Div ->
            eval01(expr.left).flatMap { left ->
                eval01(expr.right).flatMap { right ->
                    safediv0(left, right)
                }
            }
    }

// - using nullable
fun eval11(expr: Expr): Int? =
    when (expr) {
        is Val -> expr.value
        is Div -> eval10(expr.left)?.let { left ->
            eval10(expr.right)?.let { right ->
                safediv1(left, right)
            }
        }
    }
