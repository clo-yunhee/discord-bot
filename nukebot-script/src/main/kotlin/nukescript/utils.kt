package nukescript

fun <T> throwITE(): T = throw IllegalTokenException()
fun <T> throwIAE(): T = throw IllegalASTException()

fun Any?.isIn(vararg types: Any?) = types.any { this == it }
