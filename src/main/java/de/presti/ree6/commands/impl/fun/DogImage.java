package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
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

public class DogImage extends Command {

    public DogImage() {
        super("randomdog", "Shows you a Random Dog Picture!", Category.FUN, new String[] { "dog", "dogimage" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://dog.ceo/api/breeds/image/random")).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Dog Image!");
        em.setColor(BotUtil.randomEmbedColor());
        em.setImage(js.get("message").getAsString());
        em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.ADVERTISEMENT, sender.getUser().getAvatarUrl());

        sendMessage(em, m, hook);
    }
}
