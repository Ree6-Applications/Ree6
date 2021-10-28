package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Prefix extends Command {


    public Prefix() {
        super("prefix", "Change Ree6's Bot-Prefix!", Category.MOD, new String[] { "setprefix", "changeprefix" }, new CommandData("prefix", "Change Ree6's Bot-Prefix!").addOptions(new OptionData(OptionType.STRING, "name", "What should the new Prefix be?").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR) && sender.hasPermission(Permission.MANAGE_SERVER)) {
            if (args.length != 1) {
                sendMessage((args.length < 1 ? "Not enough" : "Too many") + " Arguments!", 5, m, hook);
                sendMessage("Use " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "prefix PREFIX", 5, m, hook);
            } else {
                Main.sqlWorker.setSetting(m.getGuild().getId(), "chatprefix", args[0]);
                sendMessage("Your new Prefix has been set to: " + args[0], 5, m, hook);
            }
        } else {
            sendMessage("You don't have the Permission for this Command!", 5, m, hook);
        }
    }
}
