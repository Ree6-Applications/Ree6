package de.presti.ree6.game.impl.musicquiz;

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

@GameInfo(
        name = "MusicQuiz",
        description = "game.description.musicquiz",
        minPlayers = 2,
        maxPlayers = -1)
public class MusicQuiz implements IGame {

    /**
     * The game session.
     */
    private final GameSession session;

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
     * Constructor.
     * @param gameSession The game session.
     */
    public MusicQuiz(GameSession gameSession) {
        this.session = gameSession;
        createGame();
    }

    /**
     *
     */
    @Override
    public void createGame() {
        if (session.getParticipants().isEmpty() || session.getParticipants().size() < 2) {
            Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(session.getGuild(), "message.gameCore.needMore", 2), session.getChannel());
            stopGame();
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
    }

    /**
     *
     */
    @Override
    public void startGame() {
        if (session.getGameState() == GameState.STARTED) {
            return;
        }

        session.setGameState(GameState.STARTED);

        selectNextSong();
    }

    /**
     * @param user The User who wants to join.
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
     * @param user The User who wants to leave.
     */
    @Override
    public void leaveGame(GamePlayer user) {

    }

    /**
     * @param messageReceivedEvent The Event.
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
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundTitle", currentEntry.getTitle())).queue();
        }

        if (currentEntry.checkArtist(messageContent)) {
            musicQuizPlayer.addPoints(2);
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundArtists", currentEntry.getArtist())).queue();
        }

        if (currentEntry.checkFeatures(messageContent)) {
            musicQuizPlayer.addPoints(3);
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundFeature", String.join(",", currentEntry.getFeatures()))).queue();
        }

        if (currentEntry.isTitleGuessed() && currentEntry.isArtistGuessed() && currentEntry.isFeaturesGuessed()) {
            messageReceivedEvent.getMessage().reply(LanguageService.getByGuild(messageReceivedEvent.getGuild(), "message.musicQuiz.foundAll")).queue();
            selectNextSong();
        }
    }

    /**
     * @param buttonInteractionEvent The Event.
     */
    @Override
    public void onButtonInteractionReceive(ButtonInteractionEvent buttonInteractionEvent) {
        IGame.super.onButtonInteractionReceive(buttonInteractionEvent);
    }

    /**
     *
     */
    @Override
    public void stopGame() {

    }

    public void selectNextSong() {
        currentEntry = MusicQuizUtil.getRandomEntry();

        MessageEditBuilder messageEditBuilder = new MessageEditBuilder();
        messageEditBuilder.applyMessage(menuMessage);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        embedBuilder.setDescription(LanguageService.getByGuild(session.getGuild(), "message.musicQuiz.newSong"));
        messageEditBuilder.setEmbeds(embedBuilder.build());
        messageEditBuilder.setActionRow(Button.success("game_start:" + session.getGameIdentifier(), LanguageService.getByGuild(session.getGuild(), "label.startGame")).asEnabled());
        menuMessage.editMessage(messageEditBuilder.build()).queue();

        // TODO:: play the Audio with the 10 seconds timer.
        // TODO:: also add a way to detect when those 10 seconds end and then select the next song.
        //// Main.getInstance().getMusicWorker().getGuildAudioPlayer(session.getGuild()).getPlayer().addListener();
    }

    public MusicQuizPlayer getParticipantByUserId(long userId) {
        return participants.stream().filter(c -> c.getRelatedUserId() == userId).findFirst().orElse(null);
    }
}
