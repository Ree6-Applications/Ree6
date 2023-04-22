package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * A command to give you a random pickup line
 * */

@Command(name = "pickupline", description = "command.description.pickupline", category = Category.FUN)
public class PickUpLine implements ICommand {

    // Private variable which stores the response for convenience
    private String response;

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject jsonObject = RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.dagpi.xyz/data/pickupline")
                .bearerAuth(Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken")).build()).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        // Check if there is no "category" in the JSON. If so, output the error.
        if (!jsonObject.has("category")) {
            em.setTitle("Error!");
            em.setColor(Color.RED);
            em.setDescription(commandEvent.getResource("message.default.retrievalError"));
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());
            commandEvent.reply(em.build());
            return;
        }

        // Check if the response contains any NSFW stuff and appends a warning before if so
        if(jsonObject.has("nsfw") && jsonObject.get("nsfw").getAsBoolean()) {
            response += ":red_circle: **NSFW**\n";
        }

        // Give the actual response or fail
        response += (jsonObject.has("category"))
                ? commandEvent.getResource("pickupline.response", jsonObject.get("joke").getAsString())
                : commandEvent.getResource("pickupline.no_response");
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
