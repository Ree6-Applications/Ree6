package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * A command that shows you the price of some cryptocurrencies.
 */
@Command(name = "funnycrypto", description = "Wanna see at how much the funniest cryptocurrencies are worth?", category = Category.FUN)
public class FunnyCryptocurrencies implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/doge/metrics")).getAsJsonObject();

        float dogeCoin = js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat();

        js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/btc/metrics")).getAsJsonObject();

        float bitcoin = js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat();

        js =  RequestUtility.request(new RequestUtility.Request("https://data.messari.io/api/v1/assets/ltc/metrics")).getAsJsonObject();

        float liteCoin = js.get("data").getAsJsonObject().get("market_data").getAsJsonObject().get("price_usd").getAsFloat();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle("Funniest Cryptocurrencies!");
        embedBuilder.addField("**Bitcoin**", bitcoin + " USD", true);
        embedBuilder.addField("**LiteCoin**", liteCoin + " USD", true);
        embedBuilder.addField("**DogeCoin**", dogeCoin + " USD", true);
        embedBuilder.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(embedBuilder, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "doge", "btc", "ltc", "putincoin", "crypto", "funcrypto" };
    }
}
