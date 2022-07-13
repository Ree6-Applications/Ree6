package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "randomcat", description = "Shows random Cat Images, from thecatapi.com .", category = Category.FUN)
public class CatImage implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        JsonArray js = RequestUtility.request(RequestUtility.Request.builder().url("https://api.thecatapi.com/v1/images/search").build()).getAsJsonArray();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Cat Image!");
        em.setColor(BotWorker.randomEmbedColor());
        em.setImage(js.get(0).getAsJsonObject().get("url").getAsString());
        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

        Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[] { "cat", "catimage" };
    }
}
