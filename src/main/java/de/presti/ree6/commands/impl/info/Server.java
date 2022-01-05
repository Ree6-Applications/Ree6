package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.time.format.DateTimeFormatter;

public class Server extends Command {

    public Server() {
        super("server", "Shows you Informations about your Server!", Category.INFO);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            EmbedBuilder em = new EmbedBuilder();
            em.setColor(BotUtil.randomEmbedColor());

            em.setAuthor(commandEvent.getGuild().getName(), null, commandEvent.getGuild().getIconUrl());
            em.addField(":id: **Server-ID**", commandEvent.getGuild().getId(), true);
            em.addField(":calendar: **Creation Date**", commandEvent.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), true);
            em.addField(":crown: **Owner**", commandEvent.getGuild().getOwner().getAsMention(), true);
            em.addField(":busts_in_silhouette: **Members (" + commandEvent.getGuild().getMemberCount() + ")**", "**" + (commandEvent.getGuild().getMemberCount() - (commandEvent.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot())).count()) + "** User\n**" + commandEvent.getGuild().getBoostCount() + "** Boosts :sparkles:", true);
            em.addField(":speech_balloon: **Channels (" + (commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count()) + ")**", "**" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + "** Text | **" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count() + "** Voicechannel", true);
            em.addField(":earth_africa: **Other**", "**Verificationlevel:** " + commandEvent.getGuild().getVerificationLevel().getKey(), true);
            em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

            sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else
            sendMessage("You dont have ADMINISTRATOR Permissions for this Command!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
