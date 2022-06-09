package de.presti.ree6.commands.impl.nsfw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.Neko4JsAPI;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Command(name = "nsfw", description = "Get NSFW Image for reddit.com/r/hentai", category = Category.NSFW)
public class NSFW implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getTextChannel().isNSFW()) {

            Message message = commandEvent.isSlashCommand() ?
                    commandEvent.getInteractionHook().sendMessage("Searching for Image...").complete() :
                    commandEvent.getTextChannel().sendMessage("Searching for Image...").complete();

            JsonElement jsonElement = RequestUtility.request(new RequestUtility.Request("https://www.reddit.com/r/hentai/new.json?sort=hot&limit=100"));

            if (jsonElement.isJsonObject() &&
                    jsonElement.getAsJsonObject().has("data") &&
                    jsonElement.getAsJsonObject().get("data").isJsonObject() &&
                    jsonElement.getAsJsonObject().getAsJsonObject("data").has("children") &&
                    jsonElement.getAsJsonObject().getAsJsonObject("data").get("children").isJsonArray()) {

                JsonArray children = jsonElement.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("children");

                List<String> images = new ArrayList<>();

                children.forEach(post -> {
                    if (post.isJsonObject() && post.getAsJsonObject().has("data") &&
                            post.getAsJsonObject().getAsJsonObject("data").has("url") &&
                            post.getAsJsonObject().getAsJsonObject("data").has("post_hint") &&
                            post.getAsJsonObject().getAsJsonObject("data").get("url").isJsonPrimitive() &&
                            post.getAsJsonObject().getAsJsonObject("data").get("post_hint").isJsonPrimitive() &&
                            post.getAsJsonObject().getAsJsonObject("data").getAsJsonPrimitive("url").isString() &&
                            post.getAsJsonObject().getAsJsonObject("data").getAsJsonPrimitive("post_hint").isString()) {

                        JsonObject postObject = post.getAsJsonObject().getAsJsonObject("data");

                        String postHint = postObject.getAsJsonPrimitive("post_hint").getAsString(),
                                fileUrl = postObject.getAsJsonObject("url").getAsString();
                        if ((postHint.equalsIgnoreCase("image") ||
                                postHint.equalsIgnoreCase("link") ||
                                postHint.equalsIgnoreCase("rich:video")) &&
                                !fileUrl.toLowerCase(Locale.ROOT).startsWith("https://www.reddit.com/gallery/")) {
                            images.add(fileUrl);
                        }
                    }
                });

                if (!images.isEmpty()) {
                    String randomUrl = images.get(RandomUtils.secureRandom.nextInt(images.size() - 1));
                    EmbedBuilder em = new EmbedBuilder();

                    em.setImage(randomUrl);
                    em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

                    message.editMessageEmbeds(em.build()).queue();
                } else {
                    message.editMessage("We received an empty Image list from Reddit? Please try again later!").delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
                }
            } else {
                message.editMessage("We received an Invalid response from Reddit? Please try again later!").delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
            }
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
