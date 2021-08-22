package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.api.JSONApi;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import org.json.JSONObject;

public class DogImage extends Command {

    public DogImage() {
        super("randomdog", "Shows you a Random Dog Picture!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        JSONObject js = JSONApi.GetData(JSONApi.Requests.GET, "https://dog.ceo/api/breeds/image/random");

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Dog Image!");
        em.setColor(BotUtil.randomEmbedColor());
        em.setImage(js.getString("message"));
        em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

        sendMessage(em, m);
    }
}
