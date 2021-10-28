package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class ReloadAddons extends Command {

    public ReloadAddons() {
        super("reloadaddons", "Only for the Dev", Category.HIDDEN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if(sender.getUser().getId().equalsIgnoreCase("321580743488831490")) {
            Main.addonManager.reload();
        } else {
            sendMessage("The Command " + Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "reloadaddons couldn't be found!", 5, m, hook);
        }
    }
}