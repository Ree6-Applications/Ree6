package de.presti.ree6.commands.impl.nsfw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A command to show NSFW-Image from rule34.xxx.
 */
@Command(name = "r34", description = "command.description.rule34", category = Category.NSFW)
public class Rule34 implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getChannel().getType() == ChannelType.TEXT &&
                commandEvent.getChannel().asTextChannel().isNSFW()) {

            sendMessage(commandEvent);
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.onlyNSFW"), 5);
        }
    }

    /**
     * Actual part of command handling.
     *
     * @param commandEvent the CommandEvent.
     */
    public void sendMessage(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete() :
                commandEvent.getChannel().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete();

        StringBuilder builder = new StringBuilder();
        String tags = "";

        String[] args = commandEvent.getArguments();

        if (commandEvent.isSlashCommand()) {
            OptionMapping tagsMapping = commandEvent.getOption("tags");
            if (tagsMapping != null)
                args = tagsMapping.getAsString().split(" ");
        }

        for (final String string : args)
            builder.append(string).append(' ');

        if (builder.toString().endsWith(" "))
            builder = new StringBuilder(builder.substring(0, builder.length() - 1));

        if (args.length > 0)
            tags = "&tags=" + URLEncoder.encode(builder.toString(), StandardCharsets.UTF_8).toLowerCase();

        String lowerTagTest = tags.toLowerCase().replace("1", "i").replace("0", "o").replace("3", "e").replace("4", "a").replace("5", "s").replace("7", "t");

        if (lowerTagTest.contains("loli") || lowerTagTest.contains("child") || lowerTagTest.contains("children") ||
                lowerTagTest.contains("kid") || lowerTagTest.contains("underaged") || lowerTagTest.contains("underage") ||
                lowerTagTest.contains("young") || lowerTagTest.contains("petite") || lowerTagTest.contains("toddler") ||
                lowerTagTest.contains("todler") || lowerTagTest.contains("baby")) {
            message.editMessage(commandEvent.getResource("message.nsfw.notAllowed")).queue();
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
        final JsonElement jsonElement =
                RequestUtility.requestJson(RequestUtility.Request.builder().url("https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&json=1&limit=50" + tags).build());

        if (jsonElement != null && jsonElement.isJsonArray()) {
            final JsonArray array = jsonElement.getAsJsonArray();

            if (array.size() > 0) {
                final JsonElement element = array.get(RandomUtils.secureRandom.nextInt(array.size()));

                if (element.isJsonObject()) {
                    final JsonObject object = element.getAsJsonObject();

                    if (object.has("sample_url")) {
                        EmbedBuilder em = new EmbedBuilder();
                        em.setImage(object.get("sample_url").getAsString());
                        em.setFooter(commandEvent.getMember().getEffectiveName() + " - " + BotConfig.getAdvertisement(), commandEvent.getMember().getEffectiveAvatarUrl());

                        if (commandEvent.isSlashCommand()) {
                            message.editMessage(commandEvent.getResource("message.default.checkBelow")).queue();
                            commandEvent.reply(em.build());
                        } else {
                            message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage(commandEvent.getResource("message.default.checkBelow")).queue());
                        }
                    } else {
                        message.editMessage(commandEvent.getResource("message.default.retrievalError")).queue();
                    }
                } else {
                    message.editMessage(commandEvent.getResource("message.default.retrievalError")).queue();
                }
            } else {
                message.editMessage(commandEvent.getResource("message.default.retrievalError")).queue();
            }
        } else {
            message.editMessage(commandEvent.getResource("message.default.retrievalError")).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("nsfw34", LanguageService.getDefault("command.description.rule34_slash"))
                .addOptions(new OptionData(OptionType.STRING, "tags", "Tags for the image search"));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"rule34", "34"};
    }
}