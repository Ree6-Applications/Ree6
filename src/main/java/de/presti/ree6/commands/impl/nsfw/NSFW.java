package de.presti.ree6.commands.impl.nsfw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A command to show NSFW-Image from r/hentai.
 */
@Command(name = "nsfw", description = "command.description.nsfw", category = Category.NSFW)
public class NSFW implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getChannel().getType() == ChannelType.TEXT && commandEvent.getChannel().asTextChannel().isNSFW()) {
            sendImage(commandEvent);
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.onlyNSFW"), 5);
        }
    }

    /**
     * Method called to send the Image.
     *
     * @param commandEvent the CommandEvent.
     */
    public void sendImage(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete() :
                commandEvent.getChannel().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete();

        JsonElement jsonElement = RequestUtility.requestJson(RequestUtility.Request.builder().url("https://www.reddit.com/r/hentai/new.json?sort=hot&limit=50").build());

        if (jsonElement.isJsonObject() &&
                jsonElement.getAsJsonObject().has("data") &&
                jsonElement.getAsJsonObject().get("data").isJsonObject() &&
                jsonElement.getAsJsonObject().getAsJsonObject("data").has("children") &&
                jsonElement.getAsJsonObject().getAsJsonObject("data").get("children").isJsonArray()) {

            JsonArray children = jsonElement.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("children");

            List<String> images = new ArrayList<>();

            for (JsonElement child : children) {
                if (child.isJsonObject() &&
                        child.getAsJsonObject().has("data") &&
                        child.getAsJsonObject().get("data").isJsonObject()) {

                    JsonObject data = child.getAsJsonObject().getAsJsonObject("data");

                    if (data.get("url") != null && data.get("url").isJsonPrimitive() &&
                            data.get("post_hint") != null && data.get("post_hint").isJsonPrimitive()) {
                        String postHint = data.getAsJsonPrimitive("post_hint").getAsString(),
                                fileUrl = data.getAsJsonPrimitive("url").getAsString();
                        if ((postHint.equalsIgnoreCase("image") ||
                                postHint.equalsIgnoreCase("link") ||
                                postHint.equalsIgnoreCase("rich:video")) &&
                                !fileUrl.toLowerCase(Locale.ROOT).startsWith("https://www.reddit.com/gallery/") &&
                                !fileUrl.toLowerCase(Locale.ROOT).startsWith("https://redgifs.com/")) {
                            images.add(fileUrl);
                        }
                    }
                }
            }

            if (!images.isEmpty()) {
                String randomUrl = images.get(RandomUtils.secureRandom.nextInt(images.size() - 1));
                EmbedBuilder em = new EmbedBuilder();

                em.setImage(randomUrl);
                em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

                if (commandEvent.isSlashCommand()) {
                    message.editMessage(commandEvent.getResource("message.default.checkBelow")).queue();
                    commandEvent.reply(em.build());
                } else {
                    message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage(commandEvent.getResource("message.default.checkBelow")).queue());
                }
            } else {
                message.editMessage(commandEvent.getResource("message.default.retrievalError")).delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
            }
        } else {
            message.editMessage(commandEvent.getResource("message.default.retrievalError")).delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
        }
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
        return new String[]{"givensfw", "hentai"};
    }
}
