package de.presti.ree6.commands.impl.mod;

import com.google.gson.JsonElement;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
            commandEvent.reply(commandEvent.getResource("message.default.noPermission", Permission.MANAGE_SERVER.name()), 5);
            return;
        }

        if (commandEvent.isSlashCommand()) {
            OptionMapping optionMapping = commandEvent.getSlashCommandInteractionEvent().getOption("bot");
            commandEvent.getArguments()[0] = optionMapping.getAsString();
        }

        if (commandEvent.getArguments().length == 1) {
            switch (commandEvent.getArguments()[0]) {
                case "mee6" -> importFromMee6(commandEvent);

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
                            ChatUserLevel chatUserLevel;
                            if (Main.getInstance().getSqlConnector().getSqlWorker().existsInChatLevel(commandEvent.getGuild().getId(), id.getAsString())) {
                                chatUserLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(commandEvent.getGuild().getId(), id.getAsString());
                                if (chatUserLevel.getExperience() > xp.getAsLong()) {
                                    return;
                                }
                            } else {
                                chatUserLevel = new ChatUserLevel(commandEvent.getGuild().getId(), id.getAsString(), xp.getAsLong());
                            }

                            Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(chatUserLevel);
                        }
                    }
                });
                commandEvent.reply(commandEvent.getResource("message.import.success", jsonElement.getAsJsonObject().get("players").getAsJsonArray().size()), 5);
            } else {
                commandEvent.reply(commandEvent.getResource("command.perform.error"), 5);
            }
        } else {
            int code = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("status_code") &&
                    jsonElement.getAsJsonObject().get("status_code").isJsonPrimitive() ? jsonElement.getAsJsonObject().get("status_code").getAsInt() : 0;

            String reason = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("error") &&
                    jsonElement.getAsJsonObject().get("error").isJsonObject() && jsonElement.getAsJsonObject().get("error").getAsJsonObject().has("message") ?
                    jsonElement.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString() : commandEvent.getResource("label.unknown");

            commandEvent.reply(
                    commandEvent.getResource(code == 404 ?
                            "message.import.error.noData" :
                            code == 401 ?
                                    "message.import.error.visibility" :
                                    "message.import.error.unknown", reason), 5);
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
