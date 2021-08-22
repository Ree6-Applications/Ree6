package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.api.JSONApi;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.JSONArray;

public class CatImage extends Command {

    public CatImage() {
        super("randomcat", "Shows you a Random Cat Picture!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JSONArray js = JSONApi.GetData2(JSONApi.Requests.GET, "https://api.thecatapi.com/v1/images/search");

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Cat Image!");
        em.setColor(BotUtil.randomEmbedColor());
        em.setImage(js.getJSONObject(0).getString("url"));
        em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

        sendMessage(em, m, hook);
    }
}
