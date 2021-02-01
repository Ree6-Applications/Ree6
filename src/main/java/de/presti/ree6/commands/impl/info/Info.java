package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.text.SimpleDateFormat;

public class Info extends Command {

    public Info() {
        super("info", "Shows you Informations about a User.", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
            if (args.length == 1) {
                if(messageSelf.getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, m);
                    sendMessage("Use ree!info @user", 5, m);
                } else {
                    EmbedBuilder em = new EmbedBuilder();

                    User target = messageSelf.getMentionedMembers().get(0).getUser();

                    em.setTitle(target.getAsTag(), target.getAvatarUrl());
                    em.setThumbnail(target.getAvatarUrl());

                    em.addField("**UserTag**", target.getAsTag(), true);
                    em.addField("**Created Date**", new SimpleDateFormat("dd.MM.yyyy").format(target.getTimeCreated().toLocalDate()), true);
                    em.addField("**Joined Date**", new SimpleDateFormat("dd.MM.yyyy").format(m.getGuild().getMember(target).getTimeJoined().toLocalDate()), true);

                    em.setFooter("Requested by " + sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

                    sendMessage(em, m);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!info @user", 5, m);
            }
        messageSelf.delete().queue();
    }
}
