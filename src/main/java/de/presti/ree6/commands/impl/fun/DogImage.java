package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;

public class DogImage extends Command {

    public DogImage() {
        super("randomdog", "Shows you a Random Dog Picture!", Category.FUN, new String[] { "dog", "dogimage" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://dog.ceo/api/breeds/image/random")).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Dog Image!");
        em.setColor(BotUtil.randomEmbedColor());
        em.setImage(js.get("message").getAsString());
        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

        sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
