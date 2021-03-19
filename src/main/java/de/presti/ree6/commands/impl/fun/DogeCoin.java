package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.api.JSONApi;
import de.presti.ree6.api.Requests;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

public class DogeCoin extends Command {

    public DogeCoin() {
        super("dogecoin", "Shows you the pricing of DogeCoins", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        JSONObject js = JSONApi.GetData(Requests.GET, "https://data.messari.io/api/v1/assets/doge/metrics");

        sendMessage("The Current price of DogeCoins are " + js.getJSONObject("data").getJSONObject("market_data").getFloat("price_usd") + " USD!", m);
        messageSelf.delete().queue();
    }
}
