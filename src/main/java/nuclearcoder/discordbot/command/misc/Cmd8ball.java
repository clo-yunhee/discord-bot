package nuclearcoder.discordbot.command.misc;

import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cmd8ball implements Command {

    private String[] answers = new String[20];
    private Random random = new Random();

    {
        List<String> answerList = new ArrayList<>(answers.length);

        // yes
        answerList.add("It is certain");
        answerList.add("It is decidedly so");
        answerList.add("Without a doubt");
        answerList.add("Yes, definitely");
        answerList.add("You may rely on it");
        answerList.add("As I see it, yes");
        answerList.add("Most likely");
        answerList.add("Outlook good");
        answerList.add("Yes");
        answerList.add("Signs point to yes");

        // maybe
        answerList.add("Reply hazy try again");
        answerList.add("Ask again later");
        answerList.add("Better not tell you now");
        answerList.add("Cannot predict now");
        answerList.add("Concentrate and ask again");

        // no
        answerList.add("Don't count on it");
        answerList.add("My reply is no");
        answerList.add("My sources say no");
        answerList.add("Outlook not so good");
        answerList.add("Very doubtful");

        answerList.toArray(answers);
    }

    @Override public void execute(NuclearBot bot, IMessage message, String command, String[] args)
    {
        IChannel channel = message.getChannel();
        IUser user = message.getAuthor();

        String answer = answers[random.nextInt(answers.length)];

        bot.sendMessage(channel, user.mention() + "\n:8ball: " + answer);
    }

}
