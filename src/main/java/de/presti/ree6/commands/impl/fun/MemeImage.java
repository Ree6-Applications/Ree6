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
import org.json.JSONObject;

import java.util.Random;

public class MemeImage extends Command {

    public MemeImage() {
        super("randommeme", "Shows you a Random Meme Picture!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JSONObject js = JSONApi.getData(JSONApi.Requests.GET, "https://alpha-meme-maker.herokuapp.com/");

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Meme Image!");
        em.setColor(BotUtil.randomEmbedColor());

        if (js.has("data")) {

            JSONArray jsa = js.getJSONArray("data");

            JSONObject gay = jsa.getJSONObject(new Random().nextInt(jsa.length() - 1));

            em.setImage(gay.getString("image"));
        } else {
            em.setDescription("Couldn't get the Image!");
        }

        em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());
        sendMessage(em, m, hook);

    }
}
