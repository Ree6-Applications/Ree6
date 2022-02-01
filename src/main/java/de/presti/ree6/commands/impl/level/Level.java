package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Level extends Command {

    public Level() {
        super("level", "Shows the Level of a User!", Category.LEVEL, new String[] {"lvl", "xp", "rank"}, new CommandDataImpl("level", "Shows the Level of a User!")
                .addOptions(new OptionData(OptionType.USER, "target", "Show the Level of the User.")));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendLevel(targetOption.getAsMember(), commandEvent);
            } else {
                sendLevel(commandEvent.getMember(), commandEvent);
            }
        } else {
            if (commandEvent.getArguments().length <= 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendLevel(commandEvent.getMember(), commandEvent);
                } else {
                    sendLevel(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                sendMessage("Not enough Arguments!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level or " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "level @user", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendLevel(Member member, CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        em.setThumbnail(member.getUser().getAvatarUrl());
        em.setTitle("Level");

        em.addField("Chat Level", getLevel(Main.getInstance().getSqlConnector().getSqlWorker().getChatXP(commandEvent.getGuild().getId(), member.getUser().getId())) + "", true);
        em.addBlankField(true);
        em.addField("Voice Level", getLevel(Main.getInstance().getSqlConnector().getSqlWorker().getVoiceXP(commandEvent.getGuild().getId(), member.getUser().getId())) + "", true);

        em.addField("Chat XP", getFormattedXP(Main.getInstance().getSqlConnector().getSqlWorker().getChatXP(commandEvent.getGuild().getId(), member.getUser().getId())) + "", true);
        em.addBlankField(true);
        em.addField("Voice XP", getFormattedXP(Main.getInstance().getSqlConnector().getSqlWorker().getVoiceXP(commandEvent.getGuild().getId(), member.getUser().getId())) + "", true);

        em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

        sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    public int getLevel(long xp) {
        int level = 0;

        while (xp >= 1000) {
            xp -= 1000;
            level++;
        }

        return level;
    }

    public String getFormattedXP(long xp) {
        String end;

        if(xp >= 1000000000000L) {
            end = ((xp / 1000000000000L) + "").replaceAll("l", "") + "mil";
        } else if(xp >= 1000000000) {
            end = ((xp / 1000000000) + "").replaceAll("l", "") + "mil";
        } else if(xp >= 1000000) {
            end = ((xp / 1000000) + "").replaceAll("l", "") + "mio";
        } else if(xp >= 1000) {
            end = ((xp / 1000) + "").replaceAll("l", "") + "k";
        } else {
            end = "" + xp;
        }

        return end;
    }
}
