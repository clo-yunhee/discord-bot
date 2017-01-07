package nukescript

import nukescript.lexer.Lexer
import nukescript.parser.Parser

fun main(args: Array<String>) {
    do {
        print(">>> ")

        val sb = StringBuilder()
        do {
            val string = readLine() ?: ""
            sb.append(string)
        } while (string.isNotEmpty())
        val input = sb.toString()

        val lexer = Lexer(input)
        val tokens = lexer.execute()

        val parser = Parser(tokens)
        parser.execute()

        /*print("Tree:")
        println(tree)

        val scripter = Scripter(tree)
        val answer: Any = scripter.execute()

        print("Answer:")
        println(answer)
        println()*/

    } while (true)
}
