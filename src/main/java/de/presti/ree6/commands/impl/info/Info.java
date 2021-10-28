package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import de.presti.ree6.main.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.format.DateTimeFormatter;

public class Info extends Command {

    public Info() {
        super("info", "Shows you Informations about a User.", Category.INFO, new CommandData("info", "Shows you Informations about a User.").addOptions(new OptionData(OptionType.USER, "target", "Shows you Informations about the User.").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
            if (args.length == 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m, hook);
                    sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "info @user", 5, m, hook);
                } else {
                    EmbedBuilder em = new EmbedBuilder();

                    User target = messageSelf.getMentionedMembers().get(0).getUser();

                    em.setTitle(target.getAsTag(), target.getAvatarUrl());
                    em.setThumbnail(target.getAvatarUrl());

                    em.addField("**UserTag**", target.getAsTag(), true);
                    em.addField("**Created Date**", target.getTimeCreated().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);
                    em.addField("**Joined Date**", m.getGuild().getMember(target).getTimeJoined().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);

                    em.setFooter("Requested by " + sender.getUser().getAsTag() + " - " + Data.advertisement, sender.getUser().getAvatarUrl());

                    sendMessage(em, m, hook);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m, hook);
                sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "info @user", 5, m, hook);
            }
    }
}
