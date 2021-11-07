package posidon.launcher.search.parsing

import java.util.*

class Lexer(val text: String) {

    private var position = 0

    private inline val current get() = peek(0)
    private inline fun lookAhead() = peek(1)
    private inline fun peek(offset: Int): Char {
        val i = position + offset
        return if (i >= text.length) '\u0000' else text[i]
    }

    fun nextToken(): Token {

        while (current in " \n\t\r") { position++ }

        if (current.isLetter() || current == '_') {
            return readIdentifierOrKeyword()
        }

        return when (current) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> readNumberToken()
            '+' -> Token(Token.Type.Operator, Token.PLUS).also { position++ }
            '-' -> Token(Token.Type.Operator, Token.MINUS).also { position++ }
            '*' -> Token(Token.Type.Operator, Token.TIMES).also { position++ }
            '/', ':' -> Token(Token.Type.Operator, Token.DIV).also { position++ }
            '%' -> Token(Token.Type.Operator, Token.REM).also { position++ }
            '&' -> Token(Token.Type.Operator, Token.AND).also { position++ }
            '|' -> Token(Token.Type.Operator, Token.OR).also { position++ }
            '^' -> Token(Token.Type.Operator, Token.POW).also { position++ }
            '!' -> Token(Token.Type.Operator, Token.FACTORIAL).also { position++ }
            '(', '[', '{' -> Token(Token.Type.OpenParenthesis, 0.0).also { position++ }
            ')', ']', '}' -> Token(Token.Type.ClosedParenthesis, 0.0).also { position++ }
            '\u0000' -> Token(Token.Type.EOF, 0.0).also { position++ }
            else -> Token(Token.Type.Bad, 0.0).also { position++ }
        }
    }

    private fun readNumberToken(): Token {
        val stringBuilder = StringBuilder()
        var isFloat = false
        if (current == '.') {
            isFloat = true
            stringBuilder.append('.')
            position++
        }
        if (current == '0' && !isFloat) {
            if (lookAhead() == 'x' || lookAhead() == 'b' || lookAhead() == 's') {
                stringBuilder.append('0')
                position++
                stringBuilder.append(current)
                position++
            }
        }
        loop@ while (current.isDigit()) {
            stringBuilder.append(current)
            position++
            while (current == '_') {
                position++
            }
            if (!current.isDigit()) {
                when (current) {
                    '.' -> if (isFloat) {
                        return Token(Token.Type.Bad, 0.0)
                    } else {
                        isFloat = true
                        stringBuilder.append('.')
                        position++
                    }
                    else -> if (current.isLetter()) {
                        return Token(Token.Type.Bad, 0.0)
                    }
                }
            }
        }

        val textRaw = stringBuilder.toString()
        if (isFloat) {
            val d = textRaw.toDoubleOrNull()
            if (d != null) {
                return Token(Token.Type.Number, d)
            }
        } else {
            val (radix, text) = when {
                textRaw.startsWith("0x") -> {
                    16 to textRaw.substring(2)
                }
                textRaw.startsWith("0b") -> {
                    2 to textRaw.substring(2)
                }
                textRaw.startsWith("0s") -> {
                    6 to textRaw.substring(2)
                }
                else -> 10 to textRaw
            }
            val num = text.toLongOrNull(radix)?.toDouble()
            if (num != null) {
                return Token(Token.Type.Number, num)
            }
        }
        return Token(Token.Type.Bad, 0.0)
    }

    fun readIdentifierOrKeyword(): Token {
        val start = position++
        while (current.isLetterOrDigit() || current == '_') {
            position++
        }
        when (text.substring(start, position).lowercase(Locale.US)) {
            "pi" -> return Token(Token.Type.Number, Math.PI)
            "π" -> return Token(Token.Type.Number, Math.PI)
            "e" -> return Token(Token.Type.Number, Math.E)

            "plus" -> return Token(Token.Type.Operator, Token.PLUS)
            "add" -> return Token(Token.Type.Operator, Token.PLUS)

            "minus" -> return Token(Token.Type.Operator, Token.MINUS)
            "subtract" -> return Token(Token.Type.Operator, Token.MINUS)

            "times" -> return Token(Token.Type.Operator, Token.TIMES)
            "mul" -> return Token(Token.Type.Operator, Token.TIMES)
            "multiply" -> return Token(Token.Type.Operator, Token.TIMES)
            "multiplied" -> return Token(Token.Type.Operator, Token.TIMES)

            "div" -> return Token(Token.Type.Operator, Token.DIV)
            "divide" -> return Token(Token.Type.Operator, Token.DIV)
            "divided" -> return Token(Token.Type.Operator, Token.DIV)
            "over" -> return Token(Token.Type.Operator, Token.DIV)

            "and" -> return Token(Token.Type.Operator, Token.AND)
            "or" -> return Token(Token.Type.Operator, Token.OR)
            "xor" -> return Token(Token.Type.Operator, Token.XOR)
            "rem" -> return Token(Token.Type.Operator, Token.REM)
            "remainder" -> return Token(Token.Type.Operator, Token.REM)
            "pow" -> return Token(Token.Type.Operator, Token.POW)
            "power" -> return Token(Token.Type.Operator, Token.POW)
        }
        return Token(Token.Type.Bad, 0.0)
    }
}