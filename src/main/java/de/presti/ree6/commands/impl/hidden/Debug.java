package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.sql.SQLSession;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "debug", description = "Command to create a message with debug information.", category = Category.HIDDEN)
public class Debug implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        StringBuilder debugInfo = new StringBuilder("```")
                .append("Build:").append(" ").append(BotWorker.getBuild()).append("\n")
                .append("Version:").append(" ").append(BotWorker.getVersion()).append("\n")
                .append("State:").append(" ").append(BotWorker.getState()).append("\n")
                .append("Start:").append(" ").append(BotWorker.getStartTime()).append("\n")
                .append("Commit:").append(" ").append(BotWorker.getCommit()).append("\n")
                .append("Full:").append(" ").append(BotWorker.getCommitFull()).append("\n")
                .append("Branch:").append(" ").append(BotWorker.getBranch()).append("\n")
                .append("Dirty:").append(" ").append(BotWorker.isDirty()).append("\n")
                .append("Repo:").append(" ").append(BotWorker.getRepository()).append("\n")
                .append("Database:").append(" ").append(SQLSession.getDatabaseTyp().name()).append("\n")
                .append("Java:").append(" ").append(System.getProperty("java.version")).append("\n")
                .append("OS:").append(" ").append(System.getProperty("os.name")).append("\n")
                .append("OS-Version:").append(" ").append(System.getProperty("os.version")).append("\n")
                .append("```");

        commandEvent.reply(debugInfo.toString(), 5);
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
