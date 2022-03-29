package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.format.DateTimeFormatter;

@Command(name = "server", description = "See server specific information.", category = Category.INFO)
public class Server implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            EmbedBuilder em = new EmbedBuilder();
            em.setColor(BotWorker.randomEmbedColor());

            em.setAuthor(commandEvent.getGuild().getName(), null, commandEvent.getGuild().getIconUrl());
            em.addField(":id: **Server-ID**", commandEvent.getGuild().getId(), true);
            em.addField(":calendar: **Creation Date**", commandEvent.getGuild().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), true);
            em.addField(":crown: **Owner**", commandEvent.getGuild().getOwner().getAsMention(), true);
            em.addField(":busts_in_silhouette: **Members (" + commandEvent.getGuild().getMemberCount() + ")**", "**" + (commandEvent.getGuild().getMemberCount() - (commandEvent.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot())).count()) + "** User\n**" + commandEvent.getGuild().getBoostCount() + "** Boosts :sparkles:", true);
            em.addField(":speech_balloon: **Channels (" + (commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count()) + ")**", "**" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.TEXT)).count() + "** Text | **" + commandEvent.getGuild().getChannels().stream().filter(channel -> channel.getType().equals(ChannelType.VOICE)).count() + "** Voicechannel", true);
            em.addField(":earth_africa: **Other**", "**Verificationlevel:** " + commandEvent.getGuild().getVerificationLevel().getKey(), true);
            em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

            Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else
            Main.getInstance().getCommandManager().sendMessage("You dont have ADMINISTRATOR Permissions for this Command!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
