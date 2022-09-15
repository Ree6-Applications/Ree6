package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.awt.*;
import java.time.Instant;

/**
 * Command to send an Embed.
 */
@Command(name = "embed", description = "Send an Embed!", category = Category.MOD)
public class EmbedSender implements ICommand {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply("This command is only available as slash command!");
            return;
        }

        OptionMapping title = commandEvent.getSlashCommandInteractionEvent().getOption("title");
        OptionMapping description = commandEvent.getSlashCommandInteractionEvent().getOption("description");
        OptionMapping color = commandEvent.getSlashCommandInteractionEvent().getOption("color");
        OptionMapping footer = commandEvent.getSlashCommandInteractionEvent().getOption("footer");
        OptionMapping footerIcon = commandEvent.getSlashCommandInteractionEvent().getOption("footer_icon");
        OptionMapping image = commandEvent.getSlashCommandInteractionEvent().getOption("image");
        OptionMapping thumbnail = commandEvent.getSlashCommandInteractionEvent().getOption("thumbnail");
        OptionMapping author = commandEvent.getSlashCommandInteractionEvent().getOption("author");
        OptionMapping authorUrl = commandEvent.getSlashCommandInteractionEvent().getOption("author_url");
        OptionMapping authorIcon = commandEvent.getSlashCommandInteractionEvent().getOption("author_icon");
        OptionMapping url = commandEvent.getSlashCommandInteractionEvent().getOption("url");
        OptionMapping timestamp = commandEvent.getSlashCommandInteractionEvent().getOption("timestamp");

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title != null ? title.getAsString() : "Invalid Title", url != null ? url.getAsString() : null);
        embedBuilder.setDescription(description != null ? description.getAsString() : "Invalid description");

        if (color != null) {
            embedBuilder.setColor(new Color(color.getAsInt()));
        }

        if (footer != null) {
            embedBuilder.setFooter(footer.getAsString(), footerIcon != null ? footerIcon.getAsString() : null);
        }

        if (image != null) {
            embedBuilder.setImage(image.getAsString());
        }

        if (thumbnail != null) {
            embedBuilder.setThumbnail(thumbnail.getAsString());
        }

        if (author != null) {
            embedBuilder.setAuthor(author.getAsString(), authorUrl != null ? authorUrl.getAsString() : null, authorIcon != null ? authorIcon.getAsString() : null);
        }

        if (timestamp != null) {
            embedBuilder.setTimestamp(Instant.ofEpochMilli(timestamp.getAsLong()));
        }

        Main.getInstance().getCommandManager().sendMessage(embedBuilder, commandEvent.getChannel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("embed", "Send an Embed!")
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
