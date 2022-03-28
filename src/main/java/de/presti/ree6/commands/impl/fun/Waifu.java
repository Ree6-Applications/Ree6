package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

@Command(name = "waifu", description = "Wanna see some Waifus or Husbandos?", category = Category.FUN)
public class Waifu implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject jsonObject = RequestUtility.request(new RequestUtility.Request("https://api.dagpi.xyz/data/waifu", Main.getInstance().getConfig().getConfig().getString("dagpi.apitoken"))).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        if (!jsonObject.has("series")) {
            em.setTitle("Error!");
            em.setColor(Color.RED);
            em.setDescription("There was a problem with the API-Server! If this continues please visit <https://support.ree6.de>");
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());
            Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        JsonObject jsonObject1 = jsonObject.get("series").getAsJsonObject();

        em.setImage((jsonObject.has("display_picture") ? jsonObject.get("display_picture").getAsString() : "https://images.ree6.de/notfound.png"));
        em.addField("**Character**", "``" + (jsonObject.has("name") ? jsonObject.get("name").getAsString() : "Invalid Response by API") + "``", true);
        em.addField("**From**", "``" + (jsonObject1.has("name") ? jsonObject1.get("name").getAsString() : "Invalid Response by API") + "``", true);
        if((jsonObject.has("nsfw") && jsonObject.get("nsfw").getAsBoolean())) {
            em.addField("**NSFW**", "", true);
        }
        em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

        Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
