package de.presti.ree6.commands.impl.mod;

import com.google.gson.JsonElement;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * A class used to import data from another Bot.
 */
@Command(name = "import", description = "Import data from another Bot.", category = Category.MOD)
public class Import implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping optionMapping = commandEvent.getSlashCommandInteractionEvent().getOption("bot");
            commandEvent.getArguments()[0] = optionMapping.getAsString();
        }

        if (commandEvent.getArguments().length == 1) {
            switch (commandEvent.getArguments()[0]) {
                case "mee6" -> importFromMee6(commandEvent);

                default -> commandEvent.reply("Unknown Bot!", 5);
            }
        } else {
            commandEvent.reply("Please provide a Bot you which to Import data from!", 5);
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

        JsonElement jsonElement = RequestUtility.request(request);
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

                            Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(chatUserLevel);
                        }
                    }
                });
                commandEvent.reply("Imported " + jsonElement.getAsJsonObject().get("players").getAsJsonArray().size() + " Users!", 5);
            } else {
                commandEvent.reply("Something went wrong!", 5);
            }
        } else {
            int code = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("status_code") &&
                    jsonElement.getAsJsonObject().get("status_code").isJsonPrimitive() ? jsonElement.getAsJsonObject().get("status_code").getAsInt() : 0;

            String reason = jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("error") &&
                    jsonElement.getAsJsonObject().get("error").isJsonObject() && jsonElement.getAsJsonObject().get("error").getAsJsonObject().has("message") ?
                    jsonElement.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString() : "Unknown";

            commandEvent.reply("Something went wrong!\n" +
                    (code == 404 ?
                            "Looks like Mee6 does not have any data related to this Server!" :
                            code == 401 ?
                                    "Please set your Leaderboards visibility to public!" :
                                    "Unknown error (" + reason + ")"), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("import", "Import data from another Bot.").addOption(OptionType.STRING, "bot", "The Bot you want to import data from.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
