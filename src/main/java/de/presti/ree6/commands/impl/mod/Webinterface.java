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

public class Webinterface extends Command {


    public Webinterface() {
        super("webinterface", "Get your AccesLink to the Webinterface", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR) && sender.hasPermission(Permission.MANAGE_SERVER)) {
            String authToken = RandomUtils.randomString(25);
            Main.sqlWorker.setAuthToken(sender.getGuild().getId(), authToken);
            sendMessage("https://cp.ree6.tk/?login=" + Crypter.en(sender.getGuild().getId() + "-" + authToken + ":" + sender.getUser().getId()), 5, m);
        } else {
            sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, m);
        }
        deleteMessage(messageSelf);
    }
}
