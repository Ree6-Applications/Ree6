package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.sql.SQLException;
import java.util.function.Consumer;

public class Setup extends Command {

    public Setup() {
        super("setup", "Setup the Welcome and Log Channel!", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("log")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m);
                        sendMessage("Use ree!setup log #Log-Channel", 5, m);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-Log").queue(w -> {
                            try {
                                Main.sqlWorker.setLogWebhook(sender.getGuild().getId(), w.getId(), w.getToken());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });
                        sendMessage("Log channel has been set!", 5, m);
                    }
                } else if (args[0].equalsIgnoreCase("welcome")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m);
                        sendMessage("Use ree!setup welcome #Welcome-Channel", 5, m);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-Welcome").queue(w -> {
                            try {
                                Main.sqlWorker.setWelcomeWebhook(sender.getGuild().getId(), w.getId(), w.getToken());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        });
                        sendMessage("Welcome channel has been set!", 5, m);
                    }
                } else if (args[0].equalsIgnoreCase("mute")) {
                    if (messageSelf.getMentionedRoles().isEmpty()) {
                        sendMessage("No Role mentioned!", 5, m);
                        sendMessage("Use ree!setup mute @Muterole", 5, m);
                    } else {
                        try {
                            Main.sqlWorker.setMuteRole(sender.getGuild().getId(), messageSelf.getMentionedRoles().get(0).getId());
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        sendMessage("Mute Role has been set!", 5, m);
                    }
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m);
                sendMessage("Use ree!setup log/weclome/mute #Log/#Welcome/@Mute", 5, m);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m);
        }
        messageSelf.delete().queue();
    }
}
