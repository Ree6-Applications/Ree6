package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.api.JSONApi;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.JSONObject;

public class Waifu extends Command {

    public Waifu() {
        super("waifu", "Gives you a Random Waifu!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JSONObject js = JSONApi.getData(JSONApi.Requests.GET, "https://api.dagpi.xyz/data/waifu", "", Main.config.getConfig().getString("dagpi.apitoken"));
        JSONObject array = js.getJSONObject("series");

        EmbedBuilder em = new EmbedBuilder();

        em.setImage((js.has("display_picture") ? js.getString("display_picture") : "https://images.ree6.tk/notfound.png"));
        em.addField("**Character**", "``" + (js.has("name") ? js.getString("name") : "Invalid Response by API") + "``", true);
        em.addField("**From**", "``" + (array.has("name") ? array.getString("name") : "Invalid Response by API") + "``", true);
        if((js.has("nsfw") && js.getBoolean("nsfw"))) {
            em.addField("**NSFW**", "", true);
        }
        em.setFooter(sender.getUser().getAsTag() + " - " + Data.advertisement, sender.getUser().getAvatarUrl());

        sendMessage(em, m, hook);
    }
}
