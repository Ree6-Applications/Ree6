package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class CatImage extends Command {

    public CatImage() {
        super("randomcat", "Shows you a Random Cat Picture!", Category.FUN, new String[] { "cat", "catimage" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonArray js = RequestUtility.request(new RequestUtility.Request("https://api.thecatapi.com/v1/images/search")).getAsJsonArray();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Cat Image!");
        em.setColor(BotUtil.randomEmbedColor());
        em.setImage(js.get(0).getAsJsonObject().get("url").getAsString());
        em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.ADVERTISEMENT, sender.getUser().getAvatarUrl());

        sendMessage(em, m, hook);
    }
}
