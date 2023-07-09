package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.user.User;

public class Main {
    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder()
                .setToken("#YOUR_TOKEN#")
                .addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_VOICE_STATES)
                .login().join();

        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        AudioPlayer audioPlayer = playerManager.createPlayer();

        LavaplayerAudioSource audioSource = new LavaplayerAudioSource(api, audioPlayer);

        api.addMessageCreateListener(event -> {

            User user = event.getMessageAuthor().asUser().orElse(null);
            event.getServer().ifPresent(server -> {
                user.getConnectedVoiceChannel(server).ifPresent(voiceChannel -> {
                    if (event.getMessageContent().startsWith("!play")) {
                        String query = event.getMessageContent().substring(6);

                        AudioConnection audioConnection = voiceChannel.connect().join();
                        audioConnection.setSelfMuted(false);
                        audioPlayer.setVolume(30);

                        audioConnection.setAudioSource(audioSource);

                        playerManager.loadItem(query, new AudioLoadResultHandler() {
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                audioPlayer.playTrack(track);
                            }

                            @Override
                            public void playlistLoaded(AudioPlaylist playlist) {
                            }

                            @Override
                            public void noMatches() {
                            }

                            @Override
                            public void loadFailed(FriendlyException exception) {
                            }
                        });
                    } else if (event.getMessageContent().startsWith("!stop")) {
                        audioPlayer.destroy();
                    } else if (event.getMessageContent().startsWith("!kick")) {
                        audioPlayer.destroy();
                        voiceChannel.disconnect();
                    } else if (event.getMessageContent().startsWith("!help")) {

                        ServerTextChannel serverTextChannel = event.getServerTextChannel().orElse(null);
                        long userChannelId = serverTextChannel.getId();

                        serverTextChannel.sendMessage(" !play <YT Link>     -plays video-" +
                                "                               \n!stop             -ends song-" +
                                "                               \n!kick             -kicks bot-");

                    }
                });
            });
        });

    }
}
