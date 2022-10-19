package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to get random memes.
 */
@Command(name = "randommeme", description = "command.description.meme", category = Category.FUN)
public class MemeImage implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        JsonObject js = RequestUtility.requestJson(RequestUtility.Request.builder().url("https://meme-api.herokuapp.com/gimme").build()).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle(commandEvent.getResource("command.label.randomMemeImage"));
        em.setColor(BotWorker.randomEmbedColor());

        if (js.has("url")) {
            em.setImage(js.get("url").getAsString());
        } else {
            em.setDescription(commandEvent.getResource("command.message.default.retrievalError"));
        }

        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());
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
        return new String[]{"meme", "memeimage"};
    }
}
