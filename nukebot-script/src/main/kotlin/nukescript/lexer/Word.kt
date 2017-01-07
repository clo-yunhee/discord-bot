package nukescript.lexer

import nukescript.token.Token

data class Word(val isName: Boolean, val index: Int) {
    constructor(isName: Boolean, token: Token) : this(isName, token.ordinal)
}