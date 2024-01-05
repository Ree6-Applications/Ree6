package de.presti.ree6.commands.impl.mod;

import com.google.gson.JsonElement;
import de.presti.amari4j.entities.Leaderboard;
import de.presti.amari4j.exception.InvalidAPIKeyException;
import de.presti.amari4j.exception.InvalidGuildException;
import de.presti.amari4j.exception.InvalidServerResponseException;
import de.presti.amari4j.exception.RateLimitException;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.utils.apis.AmariAPI;
import de.presti.ree6.utils.external.RequestUtility;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A class used to import data from another Bot.
 */
@Command(name = "import", description = "command.description.import", category = Category.MOD)
public class Import implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_SERVER.name()), 5);
            return;
        }

        if (commandEvent.getArguments(true).length == 1) {
            switch (commandEvent.getArguments()[0].toLowerCase()) {
                case "mee6" -> importFromMee6(commandEvent);

                case "amari" -> importFromAmari(commandEvent);

                default -> commandEvent.reply(commandEvent.getResource("message.import.unknownBot"), 5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.import.botRequired"), 5);
        }
    }

    /**
     * Sends a request to an API to get the data from Mee6.
     *
     * @param commandEvent The CommandEvent.
     */
    public void importFromMee6(CommandEvent commandEvent) {
        RequestUtility.Request request = RequestUtility.Request
                .builder()
                .url("https://mee6.xyz/api/plugins/levels/leaderboard/" + commandEvent.getGuild().getId()).build();

        JsonElement jsonElement = RequestUtility.requestJson(request);
        if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("players")) {
            JsonElement players = jsonElement.getAsJsonObject().get("players");

            if (players.isJsonArray()) {
                players.getAsJsonArray().forEach(player -> {
                    if (player.isJsonObject()) {
                        JsonElement id = player.getAsJsonObject().get("id");
                        JsonElement xp = player.getAsJsonObject().get("xp");

                        if (id.isJsonPrimitive() && xp.isJsonPrimitive()) {
                            ChatUserLevel chatUserLevel = SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getIdLong(), id.getAsLong());

                            if (chatUserLevel != null && chatUserLevel.getExperience() > xp.getAsLong()) {
                                return;
                            }

                            if (chatUserLevel == null) {
                                chatUserLevel = new ChatUserLevel(commandEvent.getGuild().getIdLong(), id.getAsLong(), xp.getAsLong());
                            } else {
                                chatUserLevel.setExperience(xp.getAsLong());
                            }

                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(chatUserLevel);
                        }
                    }
                });
                commandEvent.reply(commandEvent.getResource("message.import.success", jsonElement.getAsJsonObject().get("players").getAsJsonArray().size()), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("command.perform.error"), 5);
            }
        } else {
            int code = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("status_code") &&
                    jsonElement.getAsJsonObject().get("status_code").isJsonPrimitive() ? jsonElement.getAsJsonObject().getAsJsonPrimitive("status_code").getAsInt() : 0;

            String reason = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("error") &&
                    jsonElement.getAsJsonObject().get("error").isJsonObject() && jsonElement.getAsJsonObject().getAsJsonObject("error").has("message") ?
                    jsonElement.getAsJsonObject().getAsJsonObject("error").getAsJsonPrimitive("message").getAsString() : commandEvent.getResource("label.unknown");

            if (reason.matches("^[0-9]{2}$")) {
                code = Integer.parseInt(reason);
            }

            commandEvent.reply(
                    commandEvent.getResource(code == 404 ?
                            "message.import.error.noData" :
                            code == 401 ?
                                    "message.import.error.visibility" :
                                    "message.import.error.unknown", reason), 5);
        }
    }

    /**
     * Sends a request to an API to get the data from Amari.
     *
     * @param commandEvent The CommandEvent.
     */
    public void importFromAmari(CommandEvent commandEvent) {
        try {
            Leaderboard leaderboard = AmariAPI.getAmari4J().getRawLeaderboard(commandEvent.getGuild().getId(), Integer.MAX_VALUE);

            leaderboard.getMembers().forEach(member -> {
                ChatUserLevel chatUserLevel = SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getIdLong(), Long.parseLong(member.getUserid()));

                if (chatUserLevel != null && chatUserLevel.getExperience() > member.getExperience()) {
                    return;
                }

                if (chatUserLevel == null) {
                    chatUserLevel = new ChatUserLevel(commandEvent.getGuild().getIdLong(), Long.parseLong(member.getUserid()), member.getExperience());
                } else {
                    chatUserLevel.setExperience(member.getExperience());
                }

                SQLSession.getSqlConnector().getSqlWorker().updateEntity(chatUserLevel);
            });
            commandEvent.reply(commandEvent.getResource("message.import.success", leaderboard.getCount()), 5);
        } catch (InvalidAPIKeyException | InvalidServerResponseException | RateLimitException e) {
            // TODO:: make some extra stuff for the rate-limit.
            commandEvent.reply(commandEvent.getResource("command.perform.error"), 5);
            Sentry.captureException(e);
        } catch (InvalidGuildException e) {
            commandEvent.reply(commandEvent.getResource("message.import.error.noData"), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("import", LanguageService.getDefault("command.description.import")).addOption(OptionType.STRING, "bot", "The Bot you want to import data from.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
