package de.presti.ree6.commands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {

    void onPerform(CommandEvent commandEvent);

    CommandData getCommandData();

    String[] getAlias();

}
