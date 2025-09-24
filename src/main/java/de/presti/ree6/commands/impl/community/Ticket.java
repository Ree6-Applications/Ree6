package de.presti.ree6.commands.impl.community;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Tickets;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.EnumSet;
import java.util.Map;

/**
 * A command used to set up the Ticket system
 */
// TODO:: maybe move this to setup?
@Command(name = "tickets", description = "command.description.tickets", category = Category.COMMUNITY)
public class Ticket implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.getName()));
            return;
        }

        OptionMapping ticketChannel = commandEvent.getOption("supportchannel");
        OptionMapping logChannel = commandEvent.getOption("logchannel");

        EmbedBuilder embedBuilder = new EmbedBuilder();

        SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", commandEvent.getGuild().getId())).subscribe(tickets -> {

            tickets.ifPresent(value -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(value).block());

            StandardGuildMessageChannel channel = logChannel.getAsChannel().asStandardGuildMessageChannel();

            Tickets ticketEntity = new Tickets();
            ticketEntity.setChannelId(ticketChannel.getAsChannel().getIdLong());
            ticketEntity.setGuildId(commandEvent.getGuild().getIdLong());
            ticketEntity.setLogChannelId(logChannel.getAsChannel().getIdLong());

            channel.createWebhook("Ticket-Log").queue(webhook -> {
                ticketEntity.setLogChannelWebhookId(webhook.getIdLong());
                ticketEntity.setLogChannelWebhookToken(webhook.getToken());
                commandEvent.getGuild().createCategory("Tickets").addPermissionOverride(commandEvent.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue(category1 -> {
                    ticketEntity.setTicketCategory(category1.getIdLong());
                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(ticketEntity).block();

                    MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                    messageCreateBuilder.setEmbeds(new EmbedBuilder()
                            .setTitle(LanguageService.getByGuild(commandEvent.getGuild(), "label.openTicket").block())
                            .setDescription(SQLSession.getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getIdLong(), "message_ticket_menu").block().get().getStringValue())
                            .setColor(0x55ff00)
                            .setThumbnail(commandEvent.getGuild().getIconUrl())
                            .setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                            .build());
                    messageCreateBuilder.setComponents(ActionRow.of(Button.of(ButtonStyle.PRIMARY, "re_ticket_open", LanguageService.getByGuild(commandEvent.getGuild(), "label.openTicket").block(), Emoji.fromUnicode("U+1F4E9"))));
                    Main.getInstance().getCommandManager().sendMessage(messageCreateBuilder.build(), ticketChannel.getAsChannel().asTextChannel());
                });
            });

            embedBuilder.setDescription(commandEvent.getResource("message.ticket.setupSuccess"));
            embedBuilder.setColor(BotConfig.getMainColor());

            commandEvent.reply(embedBuilder.build());
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("tickets", "command.description.tickets")
                .addOptions(new OptionData(OptionType.CHANNEL, "supportchannel", "The channel that should have the ticket creation message.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT),
                        new OptionData(OptionType.CHANNEL, "logchannel", "The channel that should receive the transcripts.", true).setChannelTypes(ChannelType.NEWS, ChannelType.TEXT));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
