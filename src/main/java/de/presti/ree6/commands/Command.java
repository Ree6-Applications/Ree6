package de.presti.ree6.commands;

import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public abstract class Command {

    final String cmd;
    final String desc;
    final Category cat;
    String[] alias;

    final CommandData commandData;

    public Command(String command, String description, Category category) {
        cmd = command;
        desc = description;
        cat = category;
        this.commandData = new CommandDataImpl(command, description) {
        };
        alias = new String[0];
    }

    public Command(String command, String description, Category category, String[] alias) {
        cmd = command;
        desc = description;
        cat = category;
        this.alias = alias;
        this.commandData = new CommandDataImpl(command, description);
    }

    public Command(String command, String description, Category category, CommandData commandData) {
        cmd = command;
        desc = description;
        cat = category;
        this.commandData = commandData;
        alias = new String[0];
    }

    public Command(String command, String description, Category category, String[] alias, CommandData commandData) {
        cmd = command;
        desc = description;
        cat = category;
        this.alias = alias;
        this.commandData = commandData;
    }

    public abstract void onPerform(CommandEvent event);

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

    public void sendMessage(String msg, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessage(msg).queue(); else hook.sendMessage(msg).queue();
    }

    public void sendMessage(String msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        Main.getInstance().getCommandManager().sendMessage(msg, deleteSecond, m, hook);
    }


    public void sendMessage(EmbedBuilder msg, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessageEmbeds(msg.build()).queue(); else hook.sendMessageEmbeds(msg.build()).queue();
    }

    public void sendMessage(EmbedBuilder msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        Main.getInstance().getCommandManager().sendMessage(msg, deleteSecond, m, hook);
    }

    public static void deleteMessage(Message message, InteractionHook hook) {
        Main.getInstance().getCommandManager().deleteMessage(message, hook);
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

    public CommandData getCommandData() {
        return commandData;
    }
}