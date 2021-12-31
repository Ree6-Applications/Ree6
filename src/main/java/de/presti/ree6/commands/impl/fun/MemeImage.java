package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.RandomUtils;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class MemeImage extends Command {

    public MemeImage() {
        super("randommeme", "Shows you a Random Meme Picture!", Category.FUN, new String[] { "meme", "memeimage" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://meme-api.herokuapp.com/gimme")).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Meme Image!");
        em.setColor(BotUtil.randomEmbedColor());

        if (js.has("url")) {
            em.setImage(js.get("url").getAsString());
        } else {
            em.setDescription("Couldn't get the Image!");
        }

        em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.ADVERTISEMENT, sender.getUser().getAvatarUrl());
        sendMessage(em, m, hook);

    }
}
