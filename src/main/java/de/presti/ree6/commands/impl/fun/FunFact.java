package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.api.JSONApi;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.JSONObject;

public class FunFact extends Command {

    public FunFact() {
        super("funfact", "Just some random Facts!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JSONObject js = JSONApi.getData(JSONApi.Requests.GET, "https://useless-facts.sameerkumar.website/api");
        sendMessage(js.getString("data"), m, hook);
    }
}
