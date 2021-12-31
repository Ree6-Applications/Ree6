package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Waifu extends Command {

    public Waifu() {
        super("waifu", "Gives you a Random Waifu!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonObject jsonObject = new RequestUtility().request(new RequestUtility.Request("https://api.dagpi.xyz/data/waifu", Main.getInstance().getConfig().getConfig().getString("dagpi.apitoken"))).getAsJsonObject();

        JsonObject jsonObject1 = jsonObject.get("series").getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        em.setImage((jsonObject.has("display_picture") ? jsonObject.get("display_picture").getAsString() : "https://images.ree6.tk/notfound.png"));
        em.addField("**Character**", "``" + (jsonObject.has("name") ? jsonObject.get("name").getAsString() : "Invalid Response by API") + "``", true);
        em.addField("**From**", "``" + (jsonObject1.has("name") ? jsonObject1.get("name").getAsString() : "Invalid Response by API") + "``", true);
        if((jsonObject.has("nsfw") && jsonObject.get("nsfw").getAsBoolean())) {
            em.addField("**NSFW**", "", true);
        }
        em.setFooter(sender.getUser().getAsTag() + " - " + Data.ADVERTISEMENT, sender.getUser().getAvatarUrl());

        sendMessage(em, m, hook);
    }
}
