package de.presti.ree6.commands.impl;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Invite extends Command {

    public Invite() {
        super("invite", "Get a Invite Link for Ree6!", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        sendMessage(BotInfo.botInstance.getInviteUrl(Permission.ADMINISTRATOR), 5, m);
    }
}
