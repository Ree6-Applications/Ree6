package de.presti.ree6.module.game.impl.musicquiz;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.game.core.GameSession;
import de.presti.ree6.module.game.core.base.GameInfo;
import de.presti.ree6.module.game.core.base.GamePlayer;
import de.presti.ree6.module.game.core.base.GameState;
import de.presti.ree6.module.game.core.base.IGame;
import de.presti.ree6.module.game.impl.musicquiz.entities.MusicQuizEntry;
import de.presti.ree6.module.game.impl.musicquiz.entities.MusicQuizPlayer;
import de.presti.ree6.module.game.impl.musicquiz.util.MusicQuizUtil;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.utils.others.ThreadUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Class representing the game "Music Quiz"
 */
@GameInfo(
        name = "MusicQuiz",
        description = "game.description.musicquiz",
        minPlayers = 2,
        maxPlayers = -1)
public class MusicQuiz implements IGame {

    /**
     * The indicator for the current round.
     */
    int currentRound = 0;

    /**
     * Amount of rounds to play.
     */
    int maxRounds = 5;

    /**
     * The game session.
     */
    private final GameSession session;

    /**
     * List of all participants.
     */
    private final ArrayList<MusicQuizPlayer> participants = new ArrayList<>();

    /**
     * The message of the game.
     */
    Message menuMessage;

    /**
     * The current Song.
     */
    MusicQuizEntry currentEntry;

    /**
     * The Internal Timer.
     */
    Future<?> internalTimer;

    /**
     * {@link MusicQuizListener} to check if the song/timer is over.
     */
    MusicQuizListener audioEventListener;

    /**
     * Constructor.
     *
     * @param gameSession The game session.
     */
    public MusicQuiz(GameSession gameSession) {
        this.session = gameSession;
        createGame();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void createGame() {

        if (session.getHost().getVoiceState() == null || session.getHost().getVoiceState().getChannel() == null) {
            session.getChannel().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.default.notInVoiceChannel").block()).queue();
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.musicQuiz").block());
        embedBuilder.setColor(BotConfig.getMainColor());
        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.welcome", session.getGameIdentifier()).block());

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame").block()).asDisabled(),
                Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame").block()).asEnabled());
        menuMessage = session.getChannel().sendMessage(messageCreateBuilder.build()).complete();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startGame() {
        if (session.getGameState() == GameState.STARTED) {
            return;
        }

        if (session.getParticipants().isEmpty() || session.getParticipants().size() < 2) {
            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.gameCore.needMore", 2), session.getChannel());
            return;
        }

        audioEventListener = new MusicQuizListener(this);
        Main.getInstance().getMusicWorker().getGuildAudioPlayer(session.getGuild()).getPlayer().addListener(audioEventListener);

        session.setGameState(GameState.STARTED);

        selectNextSong();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void joinGame(GamePlayer user) {

        if (participants.stream().anyMatch(c -> c.getRelatedUserId() == user.getRelatedUserId())) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyJoined").block()).queue();
            return;
        }

        participants.add(new MusicQuizPlayer(user));

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder(menuMessage.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.joined").block());
        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.secondary("game_leave:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.leaveGame").block()).asEnabled());
        user.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        if (participants.size() >= 2) {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.welcome", session.getGameIdentifier()).block());
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.success("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame").block()).asEnabled(),
                    Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame").block()).asEnabled());
            menuMessage.editMessage(messageEditBuilder.build()).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void leaveGame(GamePlayer user) {
        user.getInteractionHook().deleteOriginal().queue();
        participants.removeIf(c -> c.getRelatedUserId() == user.getRelatedUserId());

        if (participants.size() <= 2) {
            MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
            messageEditBuilder.applyMessage(menuMessage);
            EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.joined").block());
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.primary("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame").block()).asDisabled(),
                    Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame").block()).asEnabled());
            menuMessage = menuMessage.editMessage(messageEditBuilder.build()).complete();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceive(MessageReceivedEvent messageReceivedEvent) {

        if (session.getGameState() != GameState.STARTED) {
            return;
        }

        if (currentEntry == null) return;

        String messageContent = messageReceivedEvent.getMessage().getContentRaw();
        MusicQuizPlayer musicQuizPlayer = getParticipantByUserId(messageReceivedEvent.getAuthor().getIdLong());

        if (musicQuizPlayer == null) return;

        if (currentEntry.checkTitle(messageContent)) {
            musicQuizPlayer.addPoints(1);

            SQLSession.getSqlConnector().getSqlWorker().getSetting(session.getGuild().getIdLong(), "configuration_rewards_musicquiz_title")
                    .subscribe(setting -> rewardPlayer(session, musicQuizPlayer, setting.get().getValue()));

            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundTitle", currentEntry.getTitle()).block()).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
            messageReceivedEvent.getMessage().delete().queue();
        }

        if (currentEntry.checkArtist(messageContent)) {
            musicQuizPlayer.addPoints(2);

            SQLSession.getSqlConnector().getSqlWorker().getSetting(session.getGuild().getIdLong(),
                            "configuration_rewards_musicquiz_artist")
                    .subscribe(setting -> rewardPlayer(session, musicQuizPlayer, setting.get().getValue()));

            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundArtists", currentEntry.getArtist()).block()).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
            messageReceivedEvent.getMessage().delete().queue();
        }

        if (currentEntry.checkFeatures(messageContent)) {
            musicQuizPlayer.addPoints(3);

            SQLSession.getSqlConnector().getSqlWorker().getSetting(session.getGuild().getIdLong(),
                    "configuration_rewards_musicquiz_feature")
                    .subscribe(setting -> rewardPlayer(session, musicQuizPlayer, setting.get().getValue()));

            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundFeature", String.join(",", currentEntry.getFeatures())).block()).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
            messageReceivedEvent.getMessage().delete().queue();
        }

        if (currentEntry.isTitleGuessed() && currentEntry.isArtistGuessed() && currentEntry.isFeaturesGuessed()) {
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundAll").block()).queue();
            selectNextSong();
        }
    }


    /**
     * @inheritDoc
     */
    @Override
    public void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent) {

        if (buttonInteractionEvent.getComponentId().equalsIgnoreCase("game_musicquiz_skip")) {
            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.skipSong"), 5, session.getChannel());
            selectNextSong();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stopGame() {
        Main.getInstance().getMusicWorker().getGuildAudioPlayer(session.getGuild()).getPlayer().removeListener(audioEventListener);
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder(menuMessage.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.newSong").block());
        List<MusicQuizPlayer> sortedList = participants.stream().sorted(Comparator.comparingInt(MusicQuizPlayer::getPoints).reversed()).toList();

        for (int i = 0; i < sortedList.size(); i++) {
            MusicQuizPlayer musicQuizPlayer = sortedList.get(i);
            embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.position", i + 1).block(),
                    LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.points", musicQuizPlayer.getRelatedUser().getAsMention(),
                            musicQuizPlayer.getPoints()).block(), false);
        }

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        menuMessage.delete().queue();
        session.getChannel().sendMessage(messageCreateBuilder.build()).queue();

        SQLSession.getSqlConnector().getSqlWorker().getSetting(session.getGuild().getIdLong(),
                "configuration_rewards_musicquiz_win")
                .subscribe(setting -> rewardPlayer(session, sortedList.get(0), setting.get().getValue()));

        Main.getInstance().getMusicWorker().disconnect(session.getGuild());
    }

    /**
     * Select a new song and play the timer!
     */
    public void selectNextSong() {
        if (session.getGameState() != GameState.STARTED) {
            return;
        }

        if (internalTimer != null && !internalTimer.isDone()) {
            internalTimer.cancel(true);
        }

        if (currentRound > 0) {
            Main.getInstance().getCommandManager()
                    .sendMessage(LanguageService.getByGuild(session.getGuild(),
                            "message.musicQuiz.finishedSong", currentEntry.getTitle(), currentEntry.getArtist()), 5, session.getChannel());
        }

        if (currentRound >= maxRounds) {
            stopGame();
            return;
        }

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(session.getGuild()).getScheduler().clearQueue();

        currentEntry = MusicQuizUtil.getRandomEntry();

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        messageEditBuilder.applyMessage(menuMessage);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.newSong").block());
        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.success("game_musicquiz_skip", LanguageService.getByGuild(session.getGuild(), "label.skip").block()).asEnabled());
        menuMessage.editMessage(messageEditBuilder.build()).queue();

        AudioChannel audioChannel = session.getGuild().getMember(session.getHost()).getVoiceState().getChannel();

        internalTimer = ThreadUtil.createThread(x -> Main.getInstance().getMusicWorker().loadAndPlay(session.getChannel(), audioChannel,
                        "storage/audio/timer.mp3", null, true, true), null, Duration.ofSeconds(10),
                false, false);

        Main.getInstance().getMusicWorker().loadAndPlay(session.getChannel(), audioChannel, currentEntry.getAudioUrl(), null, true);

        currentRound++;
    }

    /**
     * Retrieve a participant by his user id.
     *
     * @param userId The user id.
     * @return The participant or null.
     */
    public MusicQuizPlayer getParticipantByUserId(long userId) {
        return participants.stream().filter(c -> c.getRelatedUserId() == userId).findFirst().orElse(null);
    }
    private class MusicQuizListener implements lavalink.client.player.event.IPlayerEventListener.IPlayerEventListener {

        MusicQuiz musicQuiz;

        public MusicQuizListener(MusicQuiz musicQuiz) {
            this.musicQuiz = musicQuiz;
        }

        @Override
        public void onEvent(de.ree6.lavalink.player.event.PlayerEvent event) {
            if (event instanceof lavalink.client.player.event.TrackEndEvent trackEndEvent &&
                    trackEndEvent.getTrack().getInfo().title.equalsIgnoreCase("timer")) {
                musicQuiz.selectNextSong();
            }
        }
    }

}
