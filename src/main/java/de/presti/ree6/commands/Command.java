package de.presti.ree6.commands;

import de.presti.ree6.utils.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;


public abstract class Command {

    String cmd;
    String desc;
    Category cat;
    String[] alias;

    public Command(String command, String description, Category category) {
        cmd = command;
        desc = description;
        cat = category;
    }

    public Command(String command, String description, Category category, String[] alias) {
        cmd = command;
        desc = description;
        cat = category;
        this.alias = alias;
    }

    public abstract void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m);

    public String[] getAlias() {
        return alias;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDesc() {
        return desc;
    }

    public Category getCategory() { return cat; }

    public void sendMessage(String msg, MessageChannel m) {
        m.sendMessage(msg).queue();
    }

    public void sendMessage(String msg, int deletesecond, MessageChannel m) {
        m.sendMessage(msg).delay(deletesecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }


    public void sendMessage(EmbedBuilder msg, MessageChannel m) {
        m.sendMessage(msg.build()).queue();
    }

    public void sendMessage(EmbedBuilder msg, int deletesecond, MessageChannel m) {
        m.sendMessage(msg.build()).delay(deletesecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }

    public static void deleteMessage(Message message) {
        if(message != null && message.getContentRaw() != null && message.getId() != null) {
            try {
                message.delete().queue();
            } catch (Exception ex) {
                Logger.log("CommandSystem", "Couldnt delete a Message!");
            }
        }
    }

    public boolean isAlias(String arg) {
        if(getAlias() == null || getAlias().length == 0)
            return false;

        for(String alias : getAlias()) {
            if(alias.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}