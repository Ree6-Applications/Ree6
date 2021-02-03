package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.format.DateTimeFormatter;

public class Server extends Command {

    public Server() {
        super("server", "Shows you Informations about your Server!", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            EmbedBuilder em = new EmbedBuilder();

            em.setColor(BotUtil.randomEmbedColor());

            em.setAuthor(m.getGuild().getName(), null, m.getGuild().getIconUrl());
            em.addField(":id: **Server-ID**", m.getGuild().getId(), true);
            em.addField(":calendar: **Creation Date**", m.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), true);
            em.addField(":crown: **Owner**", m.getGuild().getOwner().getAsMention(), true);
            em.addField(":busts_in_silhouette: **Members (" + m.getGuild().getMemberCount() + ")**", "**" + m.getGuild().getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() + "** Online\n**" + m.getGuild().getBoostCount() + "** Boosts :sparkles:", true);
            em.addField(":speech_balloon: **Channels (" + (m.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + m.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count()) + ")**", "**" + m.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + "** Text | **" + m.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count() + "**", true);
            em.addField(":earth_africa: **Other**", "**Region:** " + ("" + m.getGuild().getRegionRaw().charAt(0)).toUpperCase() + m.getGuild().getRegionRaw().substring(1).toLowerCase() + "\n**Verificationlevel:** " + m.getGuild().getVerificationLevel().getKey(), true);

            sendMessage(em, m);
        } else {
            sendMessage("You dont have ADMINISTRATOR Permissions for this Command!", m);
        }
    }
}
