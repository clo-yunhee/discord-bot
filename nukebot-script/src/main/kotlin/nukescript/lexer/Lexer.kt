package nukescript.lexer

import nukescript.LexerException
import nukescript.token.Token
import nukescript.token.Token.*
import java.util.*
import kotlin.properties.Delegates.notNull

class Lexer(string: String) {

    private companion object {
        val ETX = 0x3C.toChar()

        val KEYWORDS = mapOf(
                "and" to AND,
                "array" to ARRAY,
                "begin" to BEGIN,
                "const" to CONST,
                "div" to DIV,
                "do" to DO,
                "else" to ELSE,
                "end" to END,
                "if" to IF,
                "mod" to MOD,
                "not" to NOT,
                "of" to OF,
                "or" to OR,
                "procedure" to PROCEDURE,
                "program" to PROGRAM,
                "record" to RECORD,
                "then" to THEN,
                "type" to TYPE,
                "var" to VAR,
                "while" to WHILE
        )

        val STD_NAMES = setOf(
                "integer", "boolean", "false", "true", "read", "write"
        )
    }

    private val input: String
    private val length: Int

    private val output: Queue<Int>
    private val symbols = SymbolTable()

    private var position: Int
    private var ch: Char by notNull()

    init {
        this.input = string.replace("\\s+".toRegex(), " ").trim() // all separators are ' ' now.
        this.length = input.length
        this.output = LinkedList()
        this.position = -1

        for ((keyword, token) in KEYWORDS)
            symbols[keyword] = token
        for (name in STD_NAMES)
            symbols[name]
    }

    fun execute(): Queue<Int> {
        nextChar()
        while (ch != ETX) nextToken()
        emit(END_TEXT)
        return output
    }

    private fun nextToken() {
        skipSeparators()

        if (ch.isLetter()) scanWord()
        else if (ch.isDigit()) scanNumeral()
        else if ('+' == ch) {
            emit(PLUS); nextChar()
        } else if ('-' == ch) {
            emit(MINUS); nextChar()
        } else if ('*' == ch) {
            emit(ASTERISK); nextChar()
        } else if ('<' == ch) {
            nextChar()
            if ('=' == ch) {
                emit(NOT_GREATER); nextChar()
            } else if ('>' == ch) {
                emit(NOT_EQUAL); nextChar()
            } else emit(LESS)
        } else if ('=' == ch) {
            emit(EQUAL); nextChar()
        } else if ('>' == ch) {
            nextChar()
            if ('=' == ch) {
                emit(NOT_GREATER); nextChar()
            } else if ('>' == ch) {
                emit(NOT_EQUAL); nextChar()
            } else emit(LESS)
        } else if (':' == ch) {
            nextChar()
            if (':' == ch) {
                emit(BECOMES); nextChar()
            } else emit(COLON)
        } else if ('(' == ch) {
            emit(LEFT_PARENTHESIS); nextChar()
        } else if (')' == ch) {
            emit(RIGHT_PARENTHESIS); nextChar()
        } else if ('[' == ch) {
            emit(LEFT_BRACKET); nextChar()
        } else if (']' == ch) {
            emit(RIGHT_BRACKET); nextChar()
        } else if (',' == ch) {
            emit(COMMA); nextChar()
        } else if ('.' == ch) {
            nextChar()
            if ('.' == ch) {
                emit(DOUBLE_DOT); nextChar()
            } else emit(PERIOD)
        } else if (';' == ch) {
            emit(SEMICOLON); nextChar()
        } else if (ch != ETX) {
            emit(UNKNOWN); nextChar()
        }
    }

    /* complex scanners */

    private fun scanNumeral() {
        var value: Int = 0
        while (ch.isDigit()) {
            val digit = ch - '0'
            if (value <= (Int.MAX_VALUE - digit) / 10) {
                value = 10 * value + digit
                nextChar()
            } else {
                throw LexerException("A numeral is outside the range 0..${Int.MAX_VALUE}")
            }
            value = 10 * value + (ch - '0')
        }
        emit(NUMERAL, value)
    }

    private fun scanWord() {
        val sb = StringBuilder()
        while (ch.isLetterOrDigit()) {
            sb.append(ch.toLowerCase())
            nextChar()
        }

        val word = symbols[sb.toString()]
        if (word.isName)
            emit(NAME)
        emit(word.index)
    }

    /* internal methods */

    private fun nextChar() {
        if (++position < length)
            ch = input[position]
        else
            ch = ETX
    }

    private fun emit(int: Int) {
        output.add(int)
    }

    private fun emit(token: Token) {
        emit(token.ordinal)
    }

    private fun emit(token: Token, int: Int) {
        emit(token)
        emit(int)
    }

    private fun comment() {
        /* here ch is '{' */
        nextChar()
        while (ch != '}' && ch != ETX) {
            if (ch == '{') comment()
            nextChar()
        }
        if (ch == '}') nextChar()
        else throw LexerException("The closing delimiter } of a comment is missing")
    }

    private fun skipSeparators() {
        while (ch == ' ' || ch == '{') {
            if (ch == ' ') nextChar()
            else comment()
        }
    }

}