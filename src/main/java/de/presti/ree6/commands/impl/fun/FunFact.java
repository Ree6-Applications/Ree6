package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.utils.external.RequestUtility;

public class FunFact extends CommandClass {

    public FunFact() {
        super("funfact", "Just some random Facts!", Category.FUN, new String[] { "randomfact", "facts" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject js = RequestUtility.request(new RequestUtility.Request("https://useless-facts.sameerkumar.website/api")).getAsJsonObject();

        sendMessage(js.get("data").getAsString(), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
