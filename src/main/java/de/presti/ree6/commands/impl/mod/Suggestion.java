package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Suggestions;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
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
import java.util.Map;

/**
 * A command used to set up the Suggestion System.
 */
@Command(name = "suggestion", description = "command.description.suggestion", category = Category.MOD)
public class Suggestion implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!BotConfig.isModuleActive("suggestions")) {
            commandEvent.reply("Suggestions module disabled!", 5);
            return;
        }

        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.ADMINISTRATOR.name()), 5);
            return;
        }

        if (commandEvent.isSlashCommand()) {
            OptionMapping channel = commandEvent.getOption("target");
            OptionMapping messageChannel = commandEvent.getOption("messagetarget");

            if (channel == null || messageChannel == null) {
                commandEvent.reply(commandEvent.getResource("message.default.noMention.channel"), 5);
                return;
            }

            createSuggestions(commandEvent, channel.getAsChannel().asGuildMessageChannel(), messageChannel.getAsChannel().asGuildMessageChannel());
        } else {
            if (commandEvent.getMessage() != null && commandEvent.getMessage().getMentions().getChannels().size() == 2) {
                createSuggestions(commandEvent, (MessageChannel) commandEvent.getMessage().getMentions().getChannels().get(0), (MessageChannel) commandEvent.getMessage().getMentions().getChannels().get(1));
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.noMention.channel"), 5);
                commandEvent.reply(commandEvent.getResource("message.default.usage", "suggestion <#suggestions-post-channel> <#suggestions-create-channel>"), 5);
            }
        }
    }

    /**
     * Create all the entries needed.
     * @param commandEvent The CommandEvent.
     * @param channel the Suggestion channel.
     * @param messageChannel the Channel for the Message.
     */
    public void createSuggestions(CommandEvent commandEvent, MessageChannel channel, MessageChannel messageChannel) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(commandEvent.getResource("label.suggestionMenu"));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setDescription(SQLSession.getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getIdLong(), "message_suggestion_menu").getStringValue());
        embedBuilder.setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl());
        messageCreateBuilder.setEmbeds(embedBuilder.build());
        messageCreateBuilder.setActionRow(Button.primary("re_suggestion", commandEvent.getResource("message.suggestion.suggestionMenuPlaceholder")));

        Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), messageChannel);

        Suggestions suggestions = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(), "FROM Suggestions WHERE guildChannelId.guildId = :id", Map.of("id", commandEvent.getGuild().getIdLong()));

        if (suggestions != null) {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(suggestions);

            suggestions.getGuildChannelId().setChannelId(channel.getIdLong());
            SQLSession.getSqlConnector().getSqlWorker().updateEntity(suggestions);
        } else {
            suggestions = new Suggestions(commandEvent.getGuild().getIdLong(), channel.getIdLong());
            SQLSession.getSqlConnector().getSqlWorker().updateEntity(suggestions);
        }

        commandEvent.reply(commandEvent.getResource("message.suggestion.success"), 5);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("suggestion", LanguageService.getDefault("command.description.suggestion"))
                .addOptions(new OptionData(OptionType.CHANNEL, "target", "The channel the suggestions should be shown in.").setChannelTypes(ChannelType.NEWS, ChannelType.TEXT).setRequired(true))
                .addOptions(new OptionData(OptionType.CHANNEL, "messagetarget", "The channel the bot should send the suggestion message to.").setChannelTypes(ChannelType.NEWS, ChannelType.TEXT).setRequired(true))
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
