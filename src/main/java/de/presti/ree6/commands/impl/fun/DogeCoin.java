package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.RequestUtility;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DogeCoin extends Command {

    public DogeCoin() {
        super("dogecoin", "Shows you the pricing of DogeCoins", Category.FUN, new String[] { "doge" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        JsonObject js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/doge/metrics")).getAsJsonObject();

        sendMessage("The Current price of DogeCoins are " + js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat() + " USD!", m, hook);
        deleteMessage(messageSelf, hook);
    }
}
