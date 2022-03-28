package de.presti.ree6.commands.impl.nsfw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.security.SecureRandom;
import java.util.Locale;

@Command(name = "nsfw", description = "Get NSFW Image for Reddit", category = Category.NSFW)
public class NSFW implements ICommand {

    // TODO gotta rework.

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getTextChannel().isNSFW()) {

            JsonElement jsonObject = RequestUtility.request(new RequestUtility.Request("https://www.reddit.com/r/hentai/new.json?sort=hot&limit=100"));

            jsonObject = jsonObject.getAsJsonObject().get("data").getAsJsonObject().get("children");

            JsonArray jsonArray = new JsonArray();

            jsonObject.getAsJsonArray().forEach(jsonElement -> {
                if (!jsonElement.getAsJsonObject().has("data") ||
                        !jsonElement.getAsJsonObject().get("data").getAsJsonObject().has("post_hint") ||
                        !jsonElement.getAsJsonObject().get("data").getAsJsonObject().has("url")) {
                    return;
                }

                String fileHint = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("post_hint").getAsString();
                String url = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("url").getAsString();

                if ((fileHint.equalsIgnoreCase("image") ||
                        fileHint.equalsIgnoreCase("link") ||
                        fileHint.equalsIgnoreCase("rich:video")) &&
                        !url.toLowerCase(Locale.ROOT).startsWith("https://www.reddit.com/gallery/")) {
                    jsonArray.add(jsonElement);
                }
            });

            jsonObject = jsonArray.get(new SecureRandom().nextInt(jsonArray.size() - 1));

            jsonObject = jsonObject.getAsJsonObject().get("data");

            EmbedBuilder em = new EmbedBuilder();

            em.setImage(jsonObject.getAsJsonObject().get("url").getAsString());
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

            Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else {
            Main.getInstance().getCommandManager().sendMessage("Only available in NSFW Channels!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[]{"givensfw", "hentai"};
    }
}
