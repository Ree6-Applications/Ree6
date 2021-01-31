package de.presti.ree6.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;


public abstract class Command {

    String cmd;
    String desc;

    public Command(String command, String description) {
        cmd = command;
        desc = description;
    }

    public abstract void onPerform(Member sender, Message messageSelf,String[] args, TextChannel m);

    public String getCmd() {
        return cmd;
    }

    public String getDesc() {
        return desc;
    }

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

}