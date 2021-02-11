package de.presti.ree6.commands.impl.fun;

import com.mysql.cj.xdevapi.JsonArray;
import de.presti.ree6.api.JSONApi;
import de.presti.ree6.api.Requests;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

public class RandomWaifu extends Command {

    public RandomWaifu() {
        super("randomwaifu", "Gives you a Random Waifu!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        JSONObject js = JSONApi.GetData(Requests.GET, "https://api.dagpi.xyz/data/waifu", "", Main.config.getConfig().getString("dagpi.apitoken"));
        JSONObject jarray = js.getJSONObject("series");
        sendMessage((js.getBoolean("nsfw") ? "||": "") + js.getString("display_picture") + (js.getBoolean("nsfw") ? "||": ""), m);
        sendMessage("You got ``" + js.getString("name") + "`` from ``" + jarray.getString("name") + "``", m);
    }
}
