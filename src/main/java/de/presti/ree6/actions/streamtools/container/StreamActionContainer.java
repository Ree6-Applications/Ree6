package de.presti.ree6.actions.streamtools.container;

import com.github.philippheuer.events4j.core.domain.Event;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.actions.ActionRunContainer;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.sql.entities.StreamAction;
import de.presti.ree6.utils.others.ThreadUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

/**
 * A Container used to store all needed Information for a StreamAction.
 */
@Slf4j
public class StreamActionContainer {

    /**
     * The Twitch Channel ID.
     */
    @Getter(AccessLevel.PUBLIC)
    String twitchChannelId;

    /**
     * The Guild.
     */
    @Getter(AccessLevel.PUBLIC)
    Guild guild;

    /**
     * The Extra Argument.
     */
    @Getter(AccessLevel.PUBLIC)
    String extraArgument;

    /**
     * The Actions.
     */
    @Getter(AccessLevel.PUBLIC)
    List<ActionRunContainer> actions = new ArrayList<>();

    /**
     * Create a new StreamActionContainer.
     *
     * @param streamAction The StreamAction to create the Container for.
     */
    public StreamActionContainer(StreamAction streamAction) {
        twitchChannelId = streamAction.getIntegration().getChannelId();
        guild = BotWorker.getShardManager().getGuildById(streamAction.getGuild());
        extraArgument = streamAction.getArgument();

        if (streamAction.getActions() != null && streamAction.getActions().isJsonArray()) {
            JsonArray jsonArray = streamAction.getActions().getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("action") &&
                        jsonObject.has("value") &&
                        jsonObject.get("action").isJsonPrimitive() &&
                        jsonObject.get("value").isJsonPrimitive()) {
                    String action = jsonObject.getAsJsonPrimitive("action").getAsString();
                    String value = jsonObject.getAsJsonPrimitive("value").getAsString();
                    String[] args = value.split("\\s+");

                    Class<? extends IStreamAction> actionClass = StreamActionContainerCreator.getAction(action);
                    if (actionClass != null) {
                        try {
                            IStreamAction streamAction1 = actionClass.getConstructor().newInstance();
                            actions.add(new ActionRunContainer(streamAction1, args));
                        } catch (Exception e) {
                            log.error("Couldn't parse Stream-action!", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Run all Actions.
     *
     * @param event The related Twitch event.
     * @param userInput The User Input.
     */
    public void runActions(Event event, String userInput) {
        ThreadUtil.createThread(x -> {
            for (ActionRunContainer action : actions) {
                String[] args = action.getArguments();

                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.contains("%user_input%")) {
                        args[i] = arg.replace("%user_input%", userInput);
                    }
                }

                if (!action.getAction().runAction(new StreamActionEvent(guild, event, action.getArguments()))) {
                    log.warn("Couldn't run StreamAction: " + action.getAction().getClass().getSimpleName() + " for Guild: " + guild.getIdLong() + " and Channel: " + twitchChannelId + "!");
                }
            }
        });
    }
}
