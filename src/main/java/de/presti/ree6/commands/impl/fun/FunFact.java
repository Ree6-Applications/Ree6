package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class FunFact extends Command {

    public FunFact() {
        super("funfact", "Just some random Facts!", Category.FUN, new String[] { "randomfact", "facts" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://useless-facts.sameerkumar.website/api")).getAsJsonObject();

        sendMessage(js.get("data").getAsString(), m, hook);
    }
}
