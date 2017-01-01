package nukeutils

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ArgumentFormatter
/**
 * Constructs an argument formatter with the specified format.
 * Tokens "$n" or "{$n}" are replaced with their corresponding argument.
 * The zero-th token is the sender's username.

 * @param format the format string
 */
(format: String) {

    private val m_formatStringArray = parse(format)
    private var m_out: StringBuilder? = null

    private fun parse(format: String): Array<FormatString> {
        val list = ArrayList<FormatString>()
        val matcher = PATTERN_ARG.matcher(format)
        val length = format.length
        var i = 0
        while (i < length) {
            if (matcher.find(i)) {
                // Anything between the start of the string and the beginning
                // of the format specifier is either fixed text or contains
                // an invalid format string.
                if (matcher.start() != i) {
                    // Assume previous characters were fixed text
                    list.add(FixedString(format.substring(i, matcher.start())))
                }

                list.add(FormatSpecifier(matcher))
                i = matcher.end()
            } else {
                // The rest of the string is fixed text
                list.add(FixedString(format.substring(i)))
                break
            }
        }
        return list.toTypedArray()
    }

    /**
     * Returns the formatted string corresponding to the given list of arguments.

     * @param username the sender's username, token zero
     * *
     * @param args     the argument array as given by the onCommand method
     * *
     * @return the formatted string
     */
    fun format(username: String, vararg args: String): String? {
        m_out = StringBuilder()
        val array = m_formatStringArray
        for (formatString in array) {
            val index = formatString.index()
            val arg: String?

            when (index) {
            // fixed text
                -2 -> arg = null
            // error in index parse
                -1 -> return null
            // sender username
                0 -> arg = username
                else -> { // regular index
                    if (index >= args.size)
                        return null
                    arg = args[index]
                }
            }
            formatString.print(arg)
        }
        return m_out!!.toString()
    }

    private interface FormatString {

        fun index(): Int

        fun print(arg: String?)

    }

    private inner class FixedString internal constructor(private val m_str: String) : FormatString {

        override fun index(): Int {
            return -2
        }

        override fun print(arg: String?) {
            m_out!!.append(m_str)
        }

    }

    private inner class FormatSpecifier internal constructor(matcher: Matcher) : FormatString {

        private var m_index: Int = 0

        init {
            var indexStr: String? = matcher.group(1)
            if (indexStr == null) {
                indexStr = matcher.group(2)
            }
            if (indexStr != null) {
                try {
                    m_index = Integer.parseInt(indexStr)
                } catch (x: NumberFormatException) {
                    m_index = -1
                }

            } else {
                m_index = -1
            }
        }

        override fun index(): Int {
            return m_index
        }

        override fun print(arg: String?) {
            m_out!!.append(arg)
        }

    }

    companion object {
        private val PATTERN_ARG = Pattern.compile("\\{\\$([0-9]+)}|\\$([0-9]+)")
    }

}
