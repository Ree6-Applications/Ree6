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

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject jsonObject = RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.dagpi.xyz/data/pickupline")
                .bearerAuth(Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken")).build()).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();
        StringBuilder response = new StringBuilder();
        String emoji = ":speaking_head:";

        if (!jsonObject.has("category")) {
            em.setTitle("Error!");
            em.setColor(Color.RED);
            em.setDescription(commandEvent.getResource("message.default.retrievalError"));
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());
            commandEvent.reply(em.build());
            return;
        }

        if(jsonObject.has("nsfw") && jsonObject.get("nsfw").getAsBoolean()) {
            response.append(commandEvent.getResource("pickupline.emojis.nsfw", ":red_circle:")).append('\n');
        }

        commandEvent.reply(response.append((jsonObject.has("category")
                ? response.append(emoji).append(" ").append(jsonObject.get("joke").getAsString()).toString()
                : commandEvent.getResource("pickupline.no_response"))).toString());

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
