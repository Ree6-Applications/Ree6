package de.presti.ree6.commands.impl.nsfw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Command(name = "rule34", description = "Get NSFW Image from rule34.xxx", category = Category.NSFW)
public class Rule34 implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getChannel() != null &&
                commandEvent.getChannel().getType() == ChannelType.TEXT &&
                commandEvent.getChannel().asTextChannel().isNSFW()) {

            sendMessage(commandEvent);
        } else {
            Main.getInstance().getCommandManager().sendMessage("Only available in NSFW Channels!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
    }

    /**
     * Actual part of command handling.
     *
     * @param commandEvent the CommandEvent.
     */
    public void sendMessage(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage("Searching for Image...").complete() :
                commandEvent.getChannel().sendMessage("Searching for Image...").complete();


        StringBuilder builder = new StringBuilder();
        String tags = "";

        String[] args = commandEvent.getArguments();

        if (commandEvent.isSlashCommand()) {
            OptionMapping tagsMapping = commandEvent.getSlashCommandInteractionEvent().getOption("tags");
            if (tagsMapping != null)
                args = tagsMapping.getAsString().split(" ");
        }

        for (final String string : args)
            builder.append(string).append(' ');

        if (builder.toString().endsWith(" "))
            builder = new StringBuilder(builder.substring(0, builder.length() - 1));

        if (args.length > 1)
            tags = "&tags=" + URLEncoder.encode(builder.toString(), StandardCharsets.UTF_8).toLowerCase();

        if (tags.contains("loli") || tags.contains("l0li") || tags.contains("lol1") || tags.contains("l0l1")) {
            message.editMessage("Please do not search for loli hentai.").queue();
            return;
        }

        sendImage(commandEvent, message, tags);
    }

    /**
     * Method called to send the Image.
     *
     * @param commandEvent the CommandEvent.
     * @param message      the Message.
     * @param tags         the Tags.
     */
    public void sendImage(CommandEvent commandEvent, Message message, String tags) {
        final JsonElement jsonElement = RequestUtility.request(new RequestUtility.Request("https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&json=1&limit=50" + tags));

        if (jsonElement != null && jsonElement.isJsonArray()) {
            final JsonArray array = jsonElement.getAsJsonArray();

            if (array.size() > 0) {
                final JsonElement element = array.get(RandomUtils.secureRandom.nextInt(array.size()));

                if (element.isJsonObject()) {
                    final JsonObject object = element.getAsJsonObject();

                    if (object.has("sample_url")) {
                        EmbedBuilder em = new EmbedBuilder();
                        em.setImage(object.get("sample_url").getAsString());
                        em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

                        if (commandEvent.isSlashCommand()) {
                            message.editMessage("Image found!").queue();
                            Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getChannel(), null);
                        } else {
                            message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage("Image found!").queue());
                        }
                    } else {
                        message.editMessage("Could not find an image.").queue();
                    }
                } else {
                    message.editMessage("Could not find an image.").queue();
                }
            } else {
                message.editMessage("Could not find an image.").queue();
            }
        } else {
            message.editMessage("Could not find an image.").queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("rule34", "Get NSFW Image from rule34.xxx")
                .addOptions(new OptionData(OptionType.STRING, "tags", "Tags for the image search"));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"r34", "34"};
    }
}