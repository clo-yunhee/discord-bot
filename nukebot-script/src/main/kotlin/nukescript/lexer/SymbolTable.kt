package nukescript.lexer

import nukescript.token.Token
import java.util.*

class SymbolTable {

    private var names: Int = 0

    private val map: MutableMap<String, Word> = HashMap()

    operator fun get(key: String): Word {
        return map.getOrPut(key, { Word(true, ++names) })
    }

    operator fun set(key: String, token: Token) {
        map[key] = Word(false, token.ordinal)
    }

}