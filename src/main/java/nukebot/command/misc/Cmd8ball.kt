package nukebot.command.misc

import nukebot.NuclearBot
import nukebot.command.Command
import nukebot.rSendMessage
import sx.blah.discord.handle.obj.IMessage
import java.util.*

class Cmd8ball : Command {

    private val random = Random()
    private val answers = arrayOf(
            // yes
            "It is certain",
            "It is decidedly so",
            "Without a doubt",
            "Yes, definitely",
            "You may rely on it",
            "As I see it, yes",
            "Most likely",
            "Outlook good",
            "Yes",
            "Signs point to yes",

            // maybe
            "Reply hazy try again",
            "Ask again later",
            "Better not tell you now",
            "Cannot predict now",
            "Concentrate and ask again",

            // no
            "Don't count on it",
            "My reply is no",
            "My sources say no",
            "Outlook not so good",
            "Very doubtful"
    )

    override fun execute(bot: NuclearBot, message: IMessage, command: String, args: Array<String>) {
        val channel = message.channel
        val user = message.author

        val answer = answers[random.nextInt(answers.size)]

        channel.rSendMessage(user.mention() + "\n:8ball: " + answer)
    }

}
