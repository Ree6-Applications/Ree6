package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.format.DateTimeFormatter;

public class Info extends CommandClass {

    public Info() {
        super("info", "Shows you Information about a User.", Category.INFO, new CommandDataImpl("info", "Shows you Information about a User.")
                .addOptions(new OptionData(OptionType.USER, "target", "The User whose profile Information you want.").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendInfo(targetOption.getAsMember(), commandEvent);
            } else {
                sendMessage("No User was given to get the Data from!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }

        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "info @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendInfo(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "info @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendInfo(Member member, CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        em.setTitle(member.getUser().getAsTag(), member.getUser().getAvatarUrl());
        em.setThumbnail(member.getUser().getAvatarUrl());

        em.addField("**UserTag**", member.getUser().getAsTag(), true);
        em.addField("**Created Date**", member.getTimeCreated().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);
        em.addField("**Joined Date**", member.getTimeJoined().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);

        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

        sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
