package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Crypter;
import de.presti.ree6.utils.RandomUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Webinterface extends Command {


    public Webinterface() {
        super("webinterface", "Get your AccesLink to the Webinterface", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR) && sender.hasPermission(Permission.MANAGE_SERVER)) {

            //TODO Rework (Privat Message)

            String authToken = RandomUtils.randomString(25);
            Main.sqlWorker.setAuthToken(sender.getGuild().getId(), authToken);
            sendMessage("https://cp.ree6.de/?login=" + Crypter.en(sender.getGuild().getId() + "-" + authToken + ":" + sender.getUser().getId()), 5, m, hook);
        } else {
            sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, m, hook);
        }
        deleteMessage(messageSelf);
    }
}
