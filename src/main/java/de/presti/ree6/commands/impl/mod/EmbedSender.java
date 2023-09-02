package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.apache.commons.validator.GenericValidator;

import java.awt.*;
import java.time.Instant;

/**
 * Command to send an Embed.
 */
@Command(name = "embed", description = "command.description.embedSender", category = Category.MOD)
public class EmbedSender implements ICommand {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MESSAGE_MANAGE.name()), 5);
            return;
        }

        OptionMapping title = commandEvent.getOption("title");
        OptionMapping description = commandEvent.getOption("description");
        OptionMapping color = commandEvent.getOption("color");
        OptionMapping footer = commandEvent.getOption("footer");
        OptionMapping footerIcon = commandEvent.getOption("footer_icon");
        OptionMapping image = commandEvent.getOption("image");
        OptionMapping thumbnail = commandEvent.getOption("thumbnail");
        OptionMapping author = commandEvent.getOption("author");
        OptionMapping authorUrl = commandEvent.getOption("author_url");
        OptionMapping authorIcon = commandEvent.getOption("author_icon");
        OptionMapping url = commandEvent.getOption("url");
        OptionMapping timestamp = commandEvent.getOption("timestamp");

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title != null ? title.getAsString() : commandEvent.getResource("label.title"), url != null ? GenericValidator.isUrl(url.getAsString()) ? url.getAsString() : null : null);
        embedBuilder.setDescription(description != null ? description.getAsString() : commandEvent.getResource("label.description"));

        if (color != null) {
            embedBuilder.setColor(new Color(color.getAsInt()));
        }

        if (footer != null) {
            embedBuilder.setFooter(footer.getAsString(), footerIcon != null ? GenericValidator.isUrl(footerIcon.getAsString()) ? footerIcon.getAsString() : null : null);
        }

        if (image != null && GenericValidator.isUrl(image.getAsString())) {
            embedBuilder.setImage(image.getAsString());
        }

        if (thumbnail != null && GenericValidator.isUrl(thumbnail.getAsString())) {
            embedBuilder.setThumbnail(thumbnail.getAsString());
        }

        if (author != null) {
            embedBuilder.setAuthor(author.getAsString(), authorUrl != null ?
                            GenericValidator.isUrl(authorUrl.getAsString()) ? authorUrl.getAsString() : null : null,
                    authorIcon != null ? GenericValidator.isUrl(authorIcon.getAsString()) ? authorIcon.getAsString() : null : null);
        }

        if (timestamp != null) {
            embedBuilder.setTimestamp(Instant.ofEpochMilli(timestamp.getAsLong()));
        }

        Main.getInstance().getCommandManager().sendMessage(embedBuilder, commandEvent.getChannel());

        if (commandEvent.isSlashCommand())
            commandEvent.reply(commandEvent.getResource("message.default.checkBelow"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("embed", LanguageService.getDefault("command.description.embedSender"))
                .addOption(OptionType.STRING, "title", "The title of the embed!", true)
                .addOption(OptionType.STRING, "description", "The description of the embed!", true)
                .addOption(OptionType.INTEGER, "color", "The color of the embed!", false)
                .addOption(OptionType.STRING, "footer", "The footer of the embed!", false)
                .addOption(OptionType.STRING, "footer_icon", "The footer icon of the embed!", false)
                .addOption(OptionType.STRING, "image", "The image of the embed!", false)
                .addOption(OptionType.STRING, "thumbnail", "The thumbnail of the embed!", false)
                .addOption(OptionType.STRING, "author", "The author of the embed!", false)
                .addOption(OptionType.STRING, "author_url", "The author url of the embed!", false)
                .addOption(OptionType.STRING, "author_icon", "The author icon of the embed!", false)
                .addOption(OptionType.STRING, "url", "The url of the embed!", false)
                .addOption(OptionType.NUMBER, "timestamp", "The timestamp of the embed!", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
