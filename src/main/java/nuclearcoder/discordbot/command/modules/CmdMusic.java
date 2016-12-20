package nuclearcoder.discordbot.command.modules;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import nuclearcoder.discordbot.BotUtil;
import nuclearcoder.discordbot.NuclearBot;
import nuclearcoder.discordbot.command.Command;
import nuclearcoder.discordbot.music.GuildMusicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CmdMusic implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdMusic.class);

    private final AudioPlayerManager playerManager;
    private final Map<String, GuildMusicManager> musicManagers;

    public CmdMusic()
    {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private static final String getInfo(AudioTrack track)
    {
        AudioTrackInfo info = track.getInfo();

        String position = BotUtil
                .stringFromTime((int) TimeUnit.MILLISECONDS.toSeconds(track.getPosition()));
        String duration = BotUtil
                .stringFromTime((int) TimeUnit.MILLISECONDS.toSeconds(track.getDuration()));

        return "**" + info.author + " - " + info.title + "** `[" + position + "/" + duration + "]`";
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild)
    {
        GuildMusicManager musicManager = musicManagers
                .computeIfAbsent(guild.getID(), k -> new GuildMusicManager(playerManager));

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
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

    private boolean joinVoiceChannel(IMessage message)
    {
        IGuild guild = message.getGuild();
        IUser user = message.getAuthor();

        List<IVoiceChannel> connectedVoiceChannels = user.getConnectedVoiceChannels();
        if (!connectedVoiceChannels.isEmpty())
        {
            final IVoiceChannel voiceChannel = getVoiceChannel(connectedVoiceChannels,
                    guild.getID());
            if (voiceChannel != null)
            {
                if (voiceChannel.isConnected())
                    return true;
                else
                {
                    try
                    {
                        voiceChannel.join();
                        return true;
                    }
                    catch (MissingPermissionsException e)
                    {
                        LOGGER.error("Couldn't join voice channel:", e);
                    }
                }
            }
        }
        return false;
    }

    @Override public void execute(NuclearBot bot, IMessage message, String originalCommand,
            String[] args)
    {
        String command = args.length > 1 ? args[1] : "";

        if (command.equalsIgnoreCase("volume"))
        {
            volume(message, args);
        }
        else if (command.equalsIgnoreCase("queue"))
        {
            queue(message);
        }
        else if (command.equalsIgnoreCase("play"))
        {
            play(message, args);
        }
        else if (command.equalsIgnoreCase("pause"))
        {
            pause(message);
        }
        else if (command.equalsIgnoreCase("skip"))
        {
            skip(message);
        }
        else if (command.equalsIgnoreCase("seek"))
        {
            seek(message, args);
        }
        else if (command.equalsIgnoreCase("leave"))
        {
            leave(bot, message);
        }
        else
        {
            help(args);
        }
    }

    private void help(String[] args)
    {
    }

    private void volume(IMessage message, String[] args)
    {
        if (args.length >= 3)
        {
            try
            {
                int volume = Integer.parseUnsignedInt(args[2]);

                getGuildAudioPlayer(message.getGuild()).player.setVolume(volume);

                BotUtil.sendMessage(message.getChannel(),
                        ":headphones: Set volume to " + volume + ".");
            }
            catch (NumberFormatException e)
            {
                // fail silently
            }
        }
    }

    private void seek(IMessage message, String[] args)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());

        AudioTrack track = musicManager.player.getPlayingTrack();
        if (track.isSeekable())
        {
            if (args.length >= 3)
            {
                int seconds = BotUtil.parseTime(args[2]);

                // in milliseconds
                track.setPosition(TimeUnit.SECONDS.toMillis(seconds));
            }
        }
    }

    private void queue(IMessage message)
    {
        IChannel channel = message.getChannel();

        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        AudioTrack playingTrack = musicManager.player.getPlayingTrack();
        Queue<AudioTrack> queue = musicManager.scheduler.getQueue();
        if (!queue.isEmpty() || playingTrack != null)
        {
            Iterator<AudioTrack> iterator = queue.iterator();
            int index = 1;

            StringBuilder sb = new StringBuilder();
            sb.append(":headphones: ̈Playing:\n");
            sb.append("Playing  ");
            sb.append(getInfo(playingTrack));
            sb.append('\n');
            while (iterator.hasNext())
            {
                sb.append(index);
                sb.append(".  ");
                sb.append(getInfo(iterator.next()));
                sb.append('\n');
                index++;
            }

            BotUtil.sendMessage(channel, sb.toString());
        }
        else
        {
            BotUtil.sendMessage(channel, ":headphones: No music in the queue. :warning: ");
        }
    }

    private void play(IMessage message, String[] args)
    {
        if (args.length >= 3)
        {
            IChannel channel = message.getChannel();
            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

            String trackURL = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            playerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
                @Override public void trackLoaded(AudioTrack track)
                {
                    if (joinVoiceChannel(message))
                    {
                        BotUtil.sendMessage(channel,
                                ":headphones: Adding to queue " + getInfo(track));
                        musicManager.scheduler.queue(track);
                    }
                }

                @Override public void playlistLoaded(AudioPlaylist playlist)
                {
                    if (joinVoiceChannel(message))
                    {
                        AudioTrack firstTrack = playlist.getSelectedTrack();

                        if (firstTrack == null)
                        {
                            firstTrack = playlist.getTracks().get(0);
                        }

                        BotUtil.sendMessage(channel,
                                ":headphones: Adding to queue " + getInfo(firstTrack)
                                        + " (first track of playlist " + playlist.getName() + ")");

                        musicManager.scheduler.queue(firstTrack);
                    }
                }

                @Override public void noMatches()
                {
                    BotUtil.sendMessage(channel, ":headphones: Nothing found by " + trackURL);
                }

                @Override public void loadFailed(FriendlyException exception)
                {
                    BotUtil.sendMessage(channel,
                            ":headphones: Could not play: " + exception.getMessage());
                }
            });
        }
    }

    private void pause(IMessage message)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.player.setPaused(!musicManager.player.isPaused());

        BotUtil.sendMessage(message.getChannel(), ":headphones: Toggled pause.");
    }

    private void skip(IMessage message)
    {
        GuildMusicManager musicManager = getGuildAudioPlayer(message.getGuild());
        musicManager.scheduler.nextTrack();

        BotUtil.sendMessage(message.getChannel(), ":headphones: Skipped to next track.");
    }

    private void leave(NuclearBot bot, IMessage message)
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
