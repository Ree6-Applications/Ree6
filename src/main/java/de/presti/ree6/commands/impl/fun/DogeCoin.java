package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "dogecoin", description = "Wanna see at how much dogecoin is worth?", category = Category.FUN)
public class DogeCoin implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/doge/metrics")).getAsJsonObject();

        Main.getInstance().getCommandManager().sendMessage("The current price of DogeCoin is " + js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat() + " USD!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[] { "doge" };
    }
}
