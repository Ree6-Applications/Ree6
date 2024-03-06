package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.others.GuildUtil;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Just a simple command which gives you a thanks messages!
 */
@Command(name = "premium", description = "Premium shit YAY", category = Category.FUN)
public class PremiumCommand implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (GuildUtil.isSupporter(commandEvent.getMember().getUser())) {
            commandEvent.setEphemeral(false);
            commandEvent.reply("Thank you for helping with funding Ree6 " + commandEvent.getMember().getAsMention() + "!");
        } else {
            commandEvent.reply("You need to be a Premium User to use this Command!");
            /*if (commandEvent.isSlashCommand()) {
                commandEvent.getSlashCommandInteractionEvent().replyWithPremiumRequired().queue();
            } else {
                commandEvent.reply("You need to be a Premium User to use this Command!");
            }*/
        }
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
