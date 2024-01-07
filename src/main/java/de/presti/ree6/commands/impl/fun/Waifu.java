package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * A command to show you are random Waifu or Husbando.
 */
@Command(name = "waifu", description = "command.description.waifu", category = Category.FUN)
public class Waifu implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        JsonObject jsonObject = RequestUtility.requestJson(RequestUtility.Request.builder()
                .url("https://api.jikan.moe/v4/random/characters")
                .build()).getAsJsonObject();

        EmbedBuilder em = new EmbedBuilder();

        if (!jsonObject.has("data")) {
            em.setTitle(commandEvent.getResource("label.error"));
            em.setColor(Color.RED);
            em.setDescription(commandEvent.getResource("message.default.retrievalError"));
            em.setFooter(commandEvent.getMember().getEffectiveName() + " - " + BotConfig.getAdvertisement(), commandEvent.getMember().getEffectiveAvatarUrl());
            commandEvent.reply(em.build());
            return;
        }

        JsonObject dataObject = jsonObject.get("data").getAsJsonObject();

        String imgUrl = "https://images.ree6.de/notfound.png";

        if (dataObject.has("images")) {
            JsonObject imagesObject = dataObject.get("images").getAsJsonObject();
            if (imagesObject.has("jpg")) {
                imgUrl = imagesObject.getAsJsonObject("jpg").getAsJsonPrimitive("image_url").getAsString();
            }
        }

        em.setImage(imgUrl);

        em.addField("**" + commandEvent.getResource("label.character") + "**", "``" + (dataObject.has("name")
                ? dataObject.getAsJsonPrimitive("name").getAsString()
                : commandEvent.getResource("message.default.retrievalError")) + "``", true);

        JsonObject fullObject = RequestUtility.requestJson(RequestUtility.Request.builder()
                .url("https://api.jikan.moe/v4/characters/" + dataObject.getAsJsonPrimitive("mal_id").getAsString() + "/full")
                .build()).getAsJsonObject();

        String from = "";

        if (fullObject.has("data")) {
            JsonObject fullDataObject = fullObject.getAsJsonObject("data");

            if (fullDataObject.has("anime")) {
                JsonArray animeArray = fullDataObject.getAsJsonArray("anime");
                if (!animeArray.isEmpty()) {
                    JsonObject animeObject = animeArray.get(0).getAsJsonObject().getAsJsonObject("anime");
                    if (animeObject.has("title")) {
                        from = "[" + animeObject.getAsJsonPrimitive("title").getAsString() + "](" + animeObject.getAsJsonPrimitive("url").getAsString() + ")";
                    }
                }
            }

            if (from.isBlank() && fullDataObject.has("manga")) {
                JsonArray mangaArray = fullDataObject.getAsJsonArray("manga");
                if (!mangaArray.isEmpty()) {
                    JsonObject mangaObject = mangaArray.get(0).getAsJsonObject().getAsJsonObject("manga");
                    if (mangaObject.has("title")) {
                        from = "[" + mangaObject.getAsJsonPrimitive("title").getAsString() + "](" + mangaObject.getAsJsonPrimitive("url").getAsString() + ")";
                    }
                }
            }
        }

        if (from.isBlank()) {
            from = commandEvent.getResource("message.default.retrievalError");
        }

        em.addField("**" + commandEvent.getResource("label.from") + "**", from, true);

        em.setFooter(commandEvent.getMember().getEffectiveName() + " - " + BotConfig.getAdvertisement(), commandEvent.getMember().getEffectiveAvatarUrl());

        commandEvent.reply(em.build());
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
        return new String[0];
    }
}