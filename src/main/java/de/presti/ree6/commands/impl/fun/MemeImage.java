package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;

public class MemeImage extends Command {

    public MemeImage() {
        super("randommeme", "Shows you a Random Meme Picture!", Category.FUN, new String[] { "meme", "memeimage" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://meme-api.herokuapp.com/gimme")).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setTitle("Random Meme Image!");
        em.setColor(BotUtil.randomEmbedColor());

        if (js.has("url")) {
            em.setImage(js.get("url").getAsString());
        } else {
            em.setDescription("Couldn't get the Image!");
        }

        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());
        sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());

    }
}
