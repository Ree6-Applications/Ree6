package de.presti.ree6.streamtools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.sql.entities.StreamAction;
import de.presti.ree6.streamtools.action.IStreamAction;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;

public class StreamActionContainer {

    @Getter(AccessLevel.PUBLIC)
    String twitchChannelId;

    @Getter(AccessLevel.PUBLIC)
    Guild guild;

    @Getter(AccessLevel.PUBLIC)
    String extraArgument;

    @Getter(AccessLevel.PUBLIC)
    String[] arguments;

    HashMap<IStreamAction, String[]> actions = new HashMap<>();

    public StreamActionContainer(StreamAction streamAction) {
        twitchChannelId = streamAction.getIntegration().getChannelId();
        guild = BotWorker.getShardManager().getGuildById(streamAction.getGuildId());
        extraArgument = streamAction.getArgument();
        for (JsonElement jsonElement : streamAction.getActions().getAsJsonArray()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("action") &&
                    jsonObject.has("value") &&
                    jsonObject.get("action").isJsonPrimitive() &&
                    jsonObject.get("value").isJsonPrimitive()) {
                String action = jsonObject.getAsJsonPrimitive("action").getAsString();
                String value = jsonObject.getAsJsonPrimitive("value").getAsString();
                String[] args = value.split(" ");
            }
        }
    }

    public void runActions(String userInput) {
        actions.forEach((action, args) -> {

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.contains("%user_input%")) {
                    args[i] = arg.replace("%user_input%", userInput);
                }
            }

            action.runAction(guild, args);
        });
    }
}
