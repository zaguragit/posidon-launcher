package posidon.launcher.search.parsing

class Token(
    val type: Type,
    val data: Double
) {

    fun getBinaryOperatorPrecedence() = if (type == Type.Operator) when (data) {
        TIMES, DIV -> 4
        PLUS, MINUS, REM -> 3
        AND -> 2
        OR -> 1
        else -> 0
    } else 0

    fun getUnaryOperatorPrecedence() = if (type == Type.Operator) when (data) {
        PLUS, MINUS, FACTORIAL -> 5
        else -> 0
    } else 0

    fun isPostUnaryOperator() = type == Type.Operator && data == FACTORIAL

    enum class Type {
        OpenParenthesis,
        ClosedParenthesis,
        Number,
        Operator,
        EOF,
        Bad
    }

    fun toString(tokenBefore: Token?) = when (type) {
        Type.OpenParenthesis -> "("
        Type.ClosedParenthesis -> ")"
        Type.Number -> data.toString()
        Type.Operator -> when (data) {
            PLUS -> if (tokenBefore?.type == Type.Number) " + " else " +"
            MINUS -> if (tokenBefore?.type == Type.Number) " - " else " -"
            TIMES -> " * "
            DIV -> " / "
            AND -> " & "
            OR -> " | "
            XOR -> " xor "
            REM -> " % "
            POW -> " ^ "
            FACTORIAL -> "! "
            else -> ""
        }
        else -> ""
    }

    companion object {
        const val PLUS        = 0.0
        const val MINUS       = 1.0
        const val TIMES       = 2.0
        const val DIV         = 3.0
        const val AND         = 4.0
        const val OR          = 5.0
        const val XOR         = 6.0
        const val REM         = 7.0
        const val POW         = 8.0
        const val FACTORIAL   = 9.0
    }
}