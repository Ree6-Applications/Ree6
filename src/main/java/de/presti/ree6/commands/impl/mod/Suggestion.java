package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.entities.SQLResponse;
import de.presti.ree6.sql.entities.Suggestions;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.awt.*;

/**
 * A command used to set up the Suggestion System.
 */
@Command(name = "suggestion", description = "Setup the Suggestion-System!", category = Category.MOD)
public class Suggestion implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            Main.getInstance().getCommandManager().sendMessage("It seems like I do not have the permissions to do that :/\nPlease re-invite me!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.isSlashCommand()) {
            OptionMapping channel = commandEvent.getSlashCommandInteractionEvent().getOption("target");
            OptionMapping messageChannel = commandEvent.getSlashCommandInteractionEvent().getOption("messageTarget");

            if (channel == null || messageChannel == null) {
                commandEvent.reply("Please provide a channel!");
                return;
            }

            createSuggestions(commandEvent, channel.getAsChannel().asGuildMessageChannel(), messageChannel.getAsChannel().asGuildMessageChannel());
        } else {
            if (commandEvent.getMessage() != null && commandEvent.getMessage().getMentions().getChannels().size() == 2) {
                createSuggestions(commandEvent, (MessageChannel) commandEvent.getMessage().getMentions().getChannels().get(0), (MessageChannel) commandEvent.getMessage().getMentions().getChannels().get(1));
            } else {
                commandEvent.reply("No channels provided!");
                commandEvent.reply("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "suggestion #suggestions-channel #message-channel");
            }
        }
    }

    /**
     * Create all the entries needed.
     * @param channel the Suggestion channel.
     * @param messageChannel the Channel for the Message.
     */
    public void createSuggestions(CommandEvent commandEvent, MessageChannel channel, MessageChannel messageChannel) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Suggestion-System");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setDescription("Click on the button below to suggest something!");
        embedBuilder.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("re_suggestion", "Suggest something!"));

        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), messageChannel);

        SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(Suggestions.class, "SELECT * FROM Suggestions WHERE guildId = ?", commandEvent.getGuild().getIdLong());

        if (sqlResponse.isSuccess()) {
            Suggestions suggestions = (Suggestions) sqlResponse.getEntity();
            Suggestions newSuggestions = new Suggestions(suggestions.getGuildId(), channel.getIdLong());

            Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(suggestions, newSuggestions, true);
        } else {
            Suggestions suggestions = new Suggestions(commandEvent.getGuild().getIdLong(), channel.getIdLong());
            Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(suggestions);
        }

        commandEvent.reply("Successfully setup the Suggestion-System!", 5);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("suggestion", "Setup the Suggestion-System!")
                .addOptions(new OptionData(OptionType.CHANNEL, "target", "The channel the suggestions should be shown in.").setRequired(true))
                .addOptions(new OptionData(OptionType.CHANNEL, "messageTarget", "The channel the bot should send the suggestion message to.").setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
