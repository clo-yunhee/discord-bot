package nuclearcoder.discordbot.command.modules;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.music.GuildMusicManager;
import nuclearcoder.util.Logger;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CmdMusic implements Command {

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;

    public CmdMusic()
    {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild)
    {
        GuildMusicManager musicManager = musicManagers
                .computeIfAbsent(guild.getID(), k -> new GuildMusicManager(playerManager));

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    private String getInfo(AudioTrack track)
    {
        return null;
    }

    @Override public void execute(NuclearBot bot, IMessage message, String originalCommand,
            String[] args)
    {
        String command = args.length > 1 ? args[1] : "";

        if (command.equalsIgnoreCase("volume"))
        {
            volume(bot, message, args);
        }
        else if (command.equalsIgnoreCase("queue"))
        {
            queue(bot, message, args);
        }
        else if (command.equalsIgnoreCase("play"))
        {
            play(bot, message, args);
        }
        else if (command.equalsIgnoreCase("pause"))
        {
            pause(bot, message, args);
        }
        else if (command.equalsIgnoreCase("skip"))
        {
            skip(bot, message, args);
        }
        else if (command.equalsIgnoreCase("seek"))
        {
            seek(bot, message, args);
        }
        else if (command.equalsIgnoreCase("leave"))
        {
            leave(bot, message, args);
        }
        else
        {
            help(bot, message, args);
        }

    }

    private void help(NuclearBot bot, IMessage message, String[] args)
    {
    }

    private IVoiceChannel getVoiceChannel(List<IVoiceChannel> voiceChannels, String guildID)
    {
        for (IVoiceChannel voiceChannel : voiceChannels)
        {
            if (voiceChannel.getGuild().getID().equals(guildID))
            {
                return voiceChannel;
            }
        }
        return null;
    }

    private boolean joinVoiceChannel(NuclearBot bot, IMessage message)
    {
        IGuild guild = message.getGuild();
        IUser user = message.getAuthor();

        List<IVoiceChannel> connectedVoiceChannels = user.getConnectedVoiceChannels();
        if (!connectedVoiceChannels.isEmpty())
        {
            final IVoiceChannel voiceChannel = getVoiceChannel(connectedVoiceChannels,
                    guild.getID());
            if (voiceChannel != null && !voiceChannel.isConnected())
            {
                try
                {
                    voiceChannel.join();
                    return true;
                }
                catch (MissingPermissionsException e)
                {
                    Logger.error("Couldn't join voice channel:");
                    Logger.printStackTrace(e);
                }
            }
        }
        return false;
    }

    private void volume(NuclearBot bot, IMessage message, String[] args)
    {
        if (args.length >= 3)
        {
            try
            {
                int volume = Integer.parseUnsignedInt(args[2]);

                getGuildAudioPlayer(message.getGuild()).player.setVolume(volume);

                bot.sendMessage(message.getChannel(), ":headphones: Set volume to " + volume + ".");
            }
            catch (NumberFormatException e)
            {
                // fail silently
            }
        }
    }

    private void seek(NuclearBot bot, IMessage message, String[] args)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());

        AudioTrack track = musicManager.player.getPlayingTrack();
        if (track.isSeekable())
        {
            if (args.length >= 3)
            {
                long position = Long.parseUnsignedLong(args[2]);

                // in milliseconds
                track.setPosition(TimeUnit.SECONDS.toMillis(position));
            }
        }
    }

    private void queue(NuclearBot bot, IMessage message, String[] args)
    {
        IChannel channel = message.getChannel();

        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        Queue<AudioTrack> queue = musicManager.scheduler.getQueue();
        if (!queue.isEmpty())
        {
            Iterator<AudioTrack> iterator = queue.iterator();
            int index = 1;

            StringBuilder sb = new StringBuilder();
            sb.append(":headphones: Queued:\n");
            sb.append("\u00A0  Next `");
            sb.append(iterator.next().getInfo().title);
            sb.append("`\n");
            while (iterator.hasNext())
            {
                sb.append("\u00A0  ");
                sb.append(index);
                sb.append(". `");
                sb.append(iterator.next().getInfo().title);
                sb.append("`\n");
                index++;
            }

            bot.sendMessage(channel, sb.toString());
        }
        else
        {
            bot.sendMessage(channel, ":headphones: No music in the queue. :warning: ");
        }
    }

    private void play(NuclearBot bot, IMessage message, String[] args)
    {
        if (args.length >= 3)
        {
            IChannel channel = message.getChannel();
            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

            String trackURL = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            playerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
                @Override public void trackLoaded(AudioTrack track)
                {
                    bot.sendMessage(channel,
                            ":headphones: Adding to queue " + track.getInfo().title);

                    joinVoiceChannel(bot, message);
                    musicManager.scheduler.queue(track);
                }

                @Override public void playlistLoaded(AudioPlaylist playlist)
                {
                    AudioTrack firstTrack = playlist.getSelectedTrack();

                    if (firstTrack == null)
                    {
                        firstTrack = playlist.getTracks().get(0);
                    }

                    bot.sendMessage(channel,
                            ":headphones: Adding to queue " + firstTrack.getInfo().title
                                    + " (first track of playlist " + playlist.getName() + ")");

                    joinVoiceChannel(bot, message);
                    musicManager.scheduler.queue(firstTrack);
                }

                @Override public void noMatches()
                {
                    bot.sendMessage(channel, ":headphones: Nothing found by " + trackURL);
                }

                @Override public void loadFailed(FriendlyException exception)
                {
                    bot.sendMessage(channel,
                            ":headphones: Could not play: " + exception.getMessage());
                }
            });
        }
    }

    private void pause(NuclearBot bot, IMessage message, String[] args)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.player.setPaused(!musicManager.player.isPaused());

        bot.sendMessage(message.getChannel(), ":headphones: Toggled pause.");
    }

    private void skip(NuclearBot bot, IMessage message, String[] args)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.scheduler.nextTrack();

        bot.sendMessage(message.getChannel(), ":headphones: Skipped to next track.");
    }

    private void leave(NuclearBot bot, IMessage message, String[] args)
    {
        IGuild guild = message.getGuild();

        List<IVoiceChannel> connectedVoiceChannels = bot.getClient().getConnectedVoiceChannels();
        if (!connectedVoiceChannels.isEmpty())
        {
            final IVoiceChannel voiceChannel = getVoiceChannel(connectedVoiceChannels,
                    guild.getID());
            if (voiceChannel != null)
            {
                voiceChannel.leave();

                String guildID = guild.getID();

                if (musicManagers.containsKey(guildID))
                {
                    musicManagers.remove(guildID).player.destroy();
                }
            }
        }
    }

}
