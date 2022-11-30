package de.presti.ree6.game.impl.musicquiz;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.game.core.GameSession;
import de.presti.ree6.game.core.base.GameInfo;
import de.presti.ree6.game.core.base.GamePlayer;
import de.presti.ree6.game.core.base.GameState;
import de.presti.ree6.game.core.base.IGame;
import de.presti.ree6.game.impl.musicquiz.entities.MusicQuizEntry;
import de.presti.ree6.game.impl.musicquiz.entities.MusicQuizPlayer;
import de.presti.ree6.game.impl.musicquiz.util.MusicQuizUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    int maxRounds = 10;

    /**
     * The game session.
     */
    private final GameSession session;

    /**
     * List of all participants.
     */
    private final  ArrayList<MusicQuizPlayer> participants = new ArrayList<>();

    /**
     * The message of the game.
     */
    Message menuMessage;

    /**
     * The current Song.
     */
    MusicQuizEntry currentEntry;

    /**
     * {@link AudioEventListener} to check if the song/timer is over.
     */
    AudioEventListener audioEventListener = event -> {
        if (event instanceof TrackEndEvent trackEndEvent) {
            if (trackEndEvent.track.getInfo().title.equalsIgnoreCase("timer")) {
                selectNextSong();
            }
        }
    };

    /**
     * Constructor.
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
            session.getChannel().sendMessage(LanguageService.getByGuild(session.getGuild(),"message.default.notInVoiceChannel")).queue();
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(LanguageService.getByGuild(session.getGuild(), "label.musicquiz"));
        embedBuilder.setColor(BotWorker.randomEmbedColor());
        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.welcome", session.getGameIdentifier()));

        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame")).asDisabled(),
                Button.secondary("game_join:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.joinGame")).asEnabled());
        session.getChannel().sendMessage(messageCreateBuilder.build()).queue(message -> menuMessage = message);

        Main.getInstance().getMusicWorker().getGuildAudioPlayer(session.getGuild()).getPlayer().addListener(audioEventListener);
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

        session.setGameState(GameState.STARTED);

        selectNextSong();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void joinGame(GamePlayer user) {

        if (participants.stream().anyMatch(c -> c.getRelatedUserId() == user.getRelatedUserId())) {
            user.getInteractionHook().editOriginal(LanguageService.getByInteraction(user.getInteractionHook().getInteraction(), "message.gameCore.alreadyJoined")).queue();
            return;
        }

        participants.add(new MusicQuizPlayer(user));

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        messageEditBuilder.applyMessage(menuMessage);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.joined"));
        messageEditBuilder.setEmbeds(embedBuilder.build());

        user.getInteractionHook().editOriginal(messageEditBuilder.build()).queue();

        if (participants.size() >= 2) {
            embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.gameCore.minimalReached"));
            messageEditBuilder.setEmbeds(embedBuilder.build());
            messageEditBuilder.setActionRow(Button.success("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame")).asEnabled());
            menuMessage.editMessage(messageEditBuilder.build()).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void leaveGame(GamePlayer user) {
        //TODO:: leave handler.
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceive(MessageReceivedEvent messageReceivedEvent) {
        IGame.super.onMessageReceive(messageReceivedEvent);

        if (session.getGameState() != GameState.STARTED) {
            return;
        }

        if (currentEntry == null) return;

        String messageContent = messageReceivedEvent.getMessage().getContentRaw();
        MusicQuizPlayer musicQuizPlayer = getParticipantByUserId(messageReceivedEvent.getAuthor().getIdLong());

        if (musicQuizPlayer == null) return;

        if (currentEntry.checkTitle(messageContent)) {
            musicQuizPlayer.addPoints(1);
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundTitle", currentEntry.getTitle())).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
        }

        if (currentEntry.checkArtist(messageContent)) {
            musicQuizPlayer.addPoints(2);
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundArtists", currentEntry.getArtist())).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
        }

        if (currentEntry.checkFeatures(messageContent)) {
            musicQuizPlayer.addPoints(3);
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundFeature", String.join(",", currentEntry.getFeatures()))).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();

        }

        if (currentEntry.isTitleGuessed() && currentEntry.isArtistGuessed() && currentEntry.isFeaturesGuessed()) {
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundAll")).queue();
            selectNextSong();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent) {
        IGame.super.onButtonInteractionReceive(buttonInteractionEvent);

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
        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        messageEditBuilder.applyMessage(menuMessage);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.newSong"));
        List<MusicQuizPlayer> sortedList = participants.stream().sorted(Comparator.comparingInt(MusicQuizPlayer::getPoints).reversed()).toList();

        for (int i = 0; i < sortedList.size(); i++) {
            MusicQuizPlayer musicQuizPlayer = sortedList.get(i);
            embedBuilder.addField(LanguageService.getByGuild(session.getGuild(), "label.position", i + 1),
                    LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.points", musicQuizPlayer.getPoints(),
                            musicQuizPlayer.getRelatedUser().getAsMention()), false);
        }

        messageEditBuilder.setEmbeds(embedBuilder.build());
        menuMessage.editMessage(messageEditBuilder.build()).queue();
    }

    /**
     * Select a new song and play the timer!
     */
    public void selectNextSong() {
        if (session.getGameState() != GameState.STARTED) {
            return;
        }

        Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.finishedSong"), 5, session.getChannel());

        if (currentRound >= maxRounds) {
            stopGame();
            return;
        }

        currentEntry = MusicQuizUtil.getRandomEntry();

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        messageEditBuilder.applyMessage(menuMessage);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.newSong"));
        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.success("game_musicquiz_skip", LanguageService.getByGuild(session.getGuild(), "label.skip")).asEnabled());
        menuMessage.editMessage(messageEditBuilder.build()).queue();

        Main.getInstance().getMusicWorker().loadAndPlay(session.getChannel(), session.getGuild().getMember(session.getHost()).getVoiceState().getChannel(),currentEntry.getAudioUrl(), null, true);
        Main.getInstance().getMusicWorker().loadAndPlay(session.getChannel(), session.getGuild().getMember(session.getHost()).getVoiceState().getChannel(),"storage/audio/timer.mp3", null, true);

        currentRound++;
    }

    /**
     * Retrieve a participant by his user id.
     * @param userId The user id.
     * @return The participant or null.
     */
    public MusicQuizPlayer getParticipantByUserId(long userId) {
        return participants.stream().filter(c -> c.getRelatedUserId() == userId).findFirst().orElse(null);
    }
}
