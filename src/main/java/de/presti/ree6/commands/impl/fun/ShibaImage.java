package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command that shows random Shiba inu images, from shibe.online
 */
@Command(name = "randomshiba", description = "command.description.shiba", category = Category.FUN)
public class ShibaImage implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonElement jsonElement = RequestUtility.requestJson(RequestUtility.Request.builder().url("https://shibe.online/api/shibes?count=1&urls=true&httpsUrls=true").build());

        if (jsonElement.isJsonObject()) {
            commandEvent.reply(commandEvent.getResource("message.default.retrievalError"), 5);
            return;
        }

        JsonArray js = jsonElement.getAsJsonArray();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle(commandEvent.getResource("label.randomShibaImage"));
        em.setColor(BotConfig.getMainColor());
        em.setImage(js.get(0).getAsString());
        em.setFooter(commandEvent.getResource("label.footerMessage", commandEvent.getMember().getEffectiveName(), BotConfig.getAdvertisement()), commandEvent.getMember().getEffectiveAvatarUrl());
        commandEvent.reply(em.build());
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
        return new String[]{"shiba", "shibaimage"};
    }
}
