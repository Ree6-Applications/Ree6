package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.main.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	public TextChannel thechannel;
	public boolean loop = false;
	public boolean shuffle = false;

	/**
	 * @param player The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only
		// if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the
		// player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}
	}

	public boolean loop() {
		return loop;
	}

	public TextChannel getThechannel() {
		return thechannel;
	}

	public void setThechannel(TextChannel thechannel) {
		this.thechannel = thechannel;
	}

	public boolean isLoop() {
		return loop;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public AudioPlayer getPlayer() {
		return player;
	}

	public BlockingQueue<AudioTrack> getQueue() {
		return queue;
	}

	public void clearqueue() {
		queue.clear();
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 * 
	 * @param channel
	 */

	public AudioTrack shuffleshit;
	
	public void randomTrack(BlockingQueue<AudioTrack> list) {

		AudioTrack track = null;
		int is = 0;
		Integer until = new Random().nextInt(list.size());
		for (int i = 0; i < until; i++) {
			track = list.peek();
			is++;
		}
		if (is == until) {
			list.remove(track);
			shuffleshit = track;
		}
	}

	public void nextRandomTrack(TextChannel channel) {
		
		randomTrack(getQueue());
		
		AudioTrack track = shuffleshit;

		if (channel != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
					BotInfo.botInstance.getSelfUser().getAvatarUrl());
			em.setTitle("Music Player!");
			em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
			em.setColor(Color.GREEN);
			em.setDescription("Next Song!\nSong: " + track.getInfo().title);
			em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

			CommandManager.sendMessage(em, 5, channel);
		}

		player.startTrack(track, false);
	}

	public void nextTrack(TextChannel channel) {
		// Start the next track, regardless of if something is already playing or not.
		// In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the
		// player.

		AudioTrack track = queue.poll();

		if (channel != null) {
			EmbedBuilder em = new EmbedBuilder();
			em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
					BotInfo.botInstance.getSelfUser().getAvatarUrl());
			em.setTitle("Music Player!");
			em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
			em.setColor(Color.GREEN);
			em.setDescription("Next Song!\nSong: " + track.getInfo().title);
			em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

			CommandManager.sendMessage(em, 5, channel);
		}

		player.startTrack(track, false);
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or
		// LOAD_FAILED)

		if (loop) {
			if (endReason == AudioTrackEndReason.LOAD_FAILED) {
				EmbedBuilder em = new EmbedBuilder();
				em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
						BotInfo.botInstance.getSelfUser().getAvatarUrl());
				em.setTitle("Music Player!");
				em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
				em.setColor(Color.RED);
				em.setDescription("Error while playing: " + track.getInfo().title + "\nError: "
						+ endReason.name().toString());
				em.setFooter(thechannel.getGuild().getName(), thechannel.getGuild().getIconUrl());

				CommandManager.sendMessage(em, 5, thechannel);

				nextTrack(thechannel);

			} else {
				player.startTrack((track.makeClone()), false);
			}
		} else if (shuffle) {
			if (endReason == AudioTrackEndReason.LOAD_FAILED) {
				EmbedBuilder em = new EmbedBuilder();
				em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
						BotInfo.botInstance.getSelfUser().getAvatarUrl());
				em.setTitle("Music Player!");
				em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
				em.setColor(Color.RED);
				em.setDescription("Error while playing: " + track.getInfo().title + "\nError: "
						+ endReason.name().toString());
				em.setFooter(thechannel.getGuild().getName(), thechannel.getGuild().getIconUrl());

				CommandManager.sendMessage(em, 5, thechannel);
			}
			nextTrack(thechannel);
		} else {

			if (endReason.mayStartNext) {

				if (endReason == AudioTrackEndReason.LOAD_FAILED) {
					EmbedBuilder em = new EmbedBuilder();
					em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
							BotInfo.botInstance.getSelfUser().getAvatarUrl());
					em.setTitle("Music Player!");
					em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
					em.setColor(Color.RED);
					em.setDescription("Error while playing: " + track.getInfo().title + "\nError: "
							+ endReason.name().toString());
					em.setFooter(thechannel.getGuild().getName(), thechannel.getGuild().getIconUrl());

					CommandManager.sendMessage(em, 5, thechannel);

				}

				nextTrack(thechannel);
			}
		}
	}
}