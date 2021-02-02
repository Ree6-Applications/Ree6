package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.api.JSONApi;
import de.presti.ree6.api.Requests;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;

public class Slap extends Command {

    public Slap() {
        super("slap", "Slap someone in the face!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (args.length == 1) {
            if(messageSelf.getMentionedMembers().isEmpty()) {
                sendMessage("No User mentioned!", 5, m);
                sendMessage("Use ree!slap @user", 5, m);
            } else {
                EmbedBuilder em = new EmbedBuilder();

                User target = messageSelf.getMentionedMembers().get(0).getUser();

                JSONObject jso = JSONApi.GetData(Requests.GET, "https://api.giphy.com/v1/gifs/random?api_key=" + Main.config.getConfig().getString("giphy.apitoken") + "&tag=slap");

                sendMessage(sender.getAsMention() + " slapped " + target.getAsMention(), m);
                sendMessage(jso.getJSONObject("data").getString("embed_url"), m);
            }
        } else {
            sendMessage("Not enough Arguments!", 5, m);
            sendMessage("Use ree!info @user", 5, m);
        }
    }
}
