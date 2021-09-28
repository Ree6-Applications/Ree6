package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotVersion;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Crypter;
import de.presti.ree6.utils.RandomUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Webinterface extends Command {


    public Webinterface() {
        super("webinterface", "Get your Access-Link to the Webinterface", Category.MOD, new String[] { "web", "interface" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR) && sender.hasPermission(Permission.MANAGE_SERVER)) {
            try {
                Main.sqlWorker.checkSettings(m.getGuild().getId());
                PrivateChannel pc = sender.getUser().openPrivateChannel().complete();
                Message message = pc.sendMessage("Here is your Login-URL: ").complete();
                String authToken = RandomUtils.randomString(25);
                Main.sqlWorker.setAuthToken(sender.getGuild().getId(), authToken);
                message.editMessage("Here is your Login-URL: " + (BotInfo.version == BotVersion.DEV ? "http://localhost:8080" : "https://cp.ree6.de") + "/?login=" + Crypter.en(sender.getGuild().getId() + "-" + authToken + ":" + sender.getUser().getId())).queue();
            } catch (Exception ex) {
                sendMessage("Hey! I couldn't send you a message please open your DMs for me so i can send you the Login-URL!", 5, m, hook);
                Main.sqlWorker.deleteAuthToken(sender.getGuild().getId());
            }
        } else {
            sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, m, hook);
        }
        deleteMessage(messageSelf);
    }
}