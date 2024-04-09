package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.GuildUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.format.DateTimeFormatter;

/**
 * A command to show you some info about the Server.
 */
@Command(name = "server", description = "command.description.server", category = Category.INFO)
public class Server implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            EmbedBuilder em = new EmbedBuilder();
            em.setColor(BotWorker.randomEmbedColor());

            Member owner = commandEvent.getGuild().getOwner();

            boolean addBadge = false;

            if (owner != null) {
                if (GuildUtil.isSupporter(owner.getUser())) {
                    addBadge = true;
                }
            }

            em.setAuthor(commandEvent.getGuild().getName(), null, commandEvent.getGuild().getIconUrl());
            em.addField(":id: **" + commandEvent.getResource("label.serverId") + "**", commandEvent.getGuild().getId(), true);
            em.addField(":calendar: **" + commandEvent.getResource("label.creationDate") + "**", commandEvent.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), true);
            em.addField(":crown: **" + commandEvent.getResource("label.owner") + "**", owner != null ? owner.getAsMention() + (addBadge ? " <a:duckswing:1070690323459735682>" : "") : commandEvent.getResource("label.notExisting"), true);
            em.addField(":busts_in_silhouette: **" + commandEvent.getResource("label.members") + " (" + commandEvent.getGuild().getMemberCount() + ")**", "**" + (commandEvent.getGuild().getMemberCount() - (commandEvent.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot())).count()) + "** User\n**" + commandEvent.getGuild().getBoostCount() + "** Boosts :sparkles:", true);
            em.addField(":speech_balloon: **" + commandEvent.getResource("label.channels") + " (" + (commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count()) + ")**", "**" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + "** Text | **" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count() + "** Voicechannel", true);
            em.addField(":earth_africa: **" + commandEvent.getResource("label.other") + "**", "**" + commandEvent.getResource("label.verificationLevel") + ":** " + commandEvent.getGuild().getVerificationLevel().getKey(), true);
            em.setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl());

            commandEvent.reply(em.build());
        } else
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission","ADMINISTRATOR"));
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
        return new String[0];
    }
}
