package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.utils.external.RequestUtility;

public class DogeCoin extends CommandClass {

    public DogeCoin() {
        super("dogecoin", "Shows you the pricing of DogeCoins", Category.FUN, new String[] { "doge" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/doge/metrics")).getAsJsonObject();

        sendMessage("The Current price of DogeCoins are " + js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat() + " USD!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
