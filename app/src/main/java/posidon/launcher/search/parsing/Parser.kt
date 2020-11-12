package posidon.launcher.search.parsing

class Parser(val text: String) {

    class SyntaxError(parser: Parser, message: String) : kotlin.Exception("$message (tokens: ${parser.tokens.joinToString(", ") { it.type.toString() }})")

    fun parseOperation(): Pair<Double, String> {
        if (tokens.find { it.type == Token.Type.Operator } == null) throw SyntaxError(this, "doesn't contain operator")
        return parseExpression() to format(tokens)
    }

    fun parseExpression() = parseBinaryExpression()

    fun format(tokens: Array<Token>): String {
        val builder = StringBuilder()
        var i = 0
        while (i < tokens.size) {
            builder.append(tokens[i].toString(if (i == 0) null else tokens[i - 1]))
            i++
        }
        return builder.toString()
    }

    private fun factorial(num: Double): Double = if (num >= 1) num * factorial(num - 1) else 1.0

    private fun calculate(left: Double, operator: Double, right: Double) = when (operator) {
        Token.TIMES -> left * right
        Token.DIV -> left / right
        Token.PLUS -> left + right
        Token.MINUS -> left - right
        Token.REM -> left % right
        Token.AND -> (left.toInt() and right.toInt()).toDouble()
        Token.OR -> (left.toInt() or right.toInt()).toDouble()
        Token.XOR -> (left.toInt() xor right.toInt()).toDouble()
        else -> throw SyntaxError(this, "unknown biOperator id: $operator")
    }

    private fun calculate(operator: Double, operand: Double) = when (operator) {
        Token.PLUS -> operand
        Token.MINUS -> -operand
        Token.FACTORIAL -> factorial(operand)
        else -> throw SyntaxError(this, "unknown unOperator id: $operator")
    }

    private var position = 0

    private val tokens: Array<Token> = run {
        val lexer = Lexer(text)
        val tokenList = ArrayList<Token>()
        while (true) {
            val token = lexer.nextToken()
            if (token.type == Token.Type.Bad) return@run arrayOf()
            tokenList.add(token)
            if (token.type == Token.Type.EOF) break
        }
        tokenList.toTypedArray()
    }

    private fun peek(offset: Int): Token {
        val index = position + offset
        return if (index >= tokens.size) {
            tokens[tokens.lastIndex]
        } else tokens[index]
    }

    private val current get() = peek(0)

    private fun match(type: Token.Type) = if (current.type == type) current.also { position++ } else throw SyntaxError(this, "matched wrong token: ${current.type}")

    private fun parseBinaryExpression(parentPrecedence: Int = 0): Double {

        val unaryOperatorPrecedence = current.getUnaryOperatorPrecedence()

        var left = if (unaryOperatorPrecedence != 0 && unaryOperatorPrecedence >= parentPrecedence) {
            val operatorToken = match(Token.Type.Operator)
            val operand = parsePostUnaryExpression(parseBinaryExpression(unaryOperatorPrecedence))
            calculate(operatorToken.data, operand)
        } else {
            parsePrimaryExpression()
        }

        while (true) {
            left = parsePostUnaryExpression(left)
            val precedence = current.getBinaryOperatorPrecedence()
            if (precedence == 0 || precedence <= parentPrecedence)
                break
            val operatorToken = match(Token.Type.Operator)
            val right = parseBinaryExpression(precedence)
            left = calculate(left, operatorToken.data, right)
        }

        return left
    }

    private fun parsePostUnaryExpression(pre: Double): Double {
        if (current.isPostUnaryOperator()) {
            return calculate(current.data, pre).also { position++ }
        }
        return pre
    }

    private fun parsePrimaryExpression() = when (current.type) {
        Token.Type.OpenParenthesis -> parseParenthesizedExpression()
        Token.Type.Number -> current.data.also { position++ }
        else -> throw SyntaxError(this, "incorrect token: ${current.type}")
    }

    private fun parseParenthesizedExpression(): Double {
        match(Token.Type.OpenParenthesis)
        val expression = parseExpression()
        match(Token.Type.ClosedParenthesis)
        return expression
    }
}