package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Setup extends Command {

    public Setup() {
        super("setup", "Setup the Welcome and Log Channel!", Category.MOD);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR)) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("log")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m, hook);
                        sendMessage("Use ree!setup log #Log-Channel", 5, m, hook);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-Log").queue(w -> Main.sqlWorker.setLogWebhook(sender.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("Log channel has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("welcome")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m, hook);
                        sendMessage("Use ree!setup welcome #Welcome-Channel", 5, m, hook);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-Welcome").queue(w -> Main.sqlWorker.setWelcomeWebhook(sender.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("Welcome channel has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("mute")) {
                    if (messageSelf.getMentionedRoles().isEmpty()) {
                        sendMessage("No Role mentioned!", 5, m, hook);
                        sendMessage("Use ree!setup mute @Muterole", 5, m, hook);
                    } else {
                        Main.sqlWorker.setMuteRole(sender.getGuild().getId(), messageSelf.getMentionedRoles().get(0).getId());
                        sendMessage("Mute Role has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("autorole")) {
                    if (args.length == 3) {
                        if (messageSelf.getMentionedRoles().isEmpty()) {
                            sendMessage("No Role mentioned!", 5, m, hook);
                            sendMessage("Use ree!setup autorole add/remove @role", 5, m, hook);
                        } else {
                            if (args[1].equalsIgnoreCase("add")) {
                                Main.sqlWorker.addAutoRole(m.getGuild().getId(), messageSelf.getMentionedRoles().get(0).getId());
                                sendMessage("Autorole has been added!", 5, m, hook);
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                Main.sqlWorker.removeAutoRole(m.getGuild().getId(), messageSelf.getMentionedRoles().get(0).getId());
                                sendMessage("Autorole has been removed!", 5, m, hook);
                            } else {
                                sendMessage("Use ree!setup autorole add/remove @role", 5, m, hook);
                            }
                        }
                    } else {
                        sendMessage("Not enough Arguments!", 5, m, hook);
                        sendMessage("Use ree!setup autorole add/remove @role", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("news")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m, hook);
                        sendMessage("Use ree!setup news #Ree6-News", 5, m, hook);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-News").queue(w -> Main.sqlWorker.setNewsWebhook(sender.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("News channel has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("join")) {
                    if (args.length == 1) {
                        sendMessage("No Message given!", 5, m, hook);
                        sendMessage("Use ree!join Your Join Message", 5, m, hook);
                        sendMessage("Usable Syntaxes: %user_name%, %guild_name%, %user_mention%", 5, m, hook);
                    } else {
                        StringBuilder message = new StringBuilder();

                        for (int i = 1; i < args.length; i++) {
                            message.append(args[i]);
                            message.append(" ");
                        }

                        if (message.length() >= 250) {
                            sendMessage("Your Welcome Message cant be longer than 250", 5, m, hook);
                            return;
                        }

                        Main.sqlWorker.setMessage(m.getGuild().getId(), message.toString());

                        sendMessage("Join Message has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("r6")) {
                    if (messageSelf.getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, m, hook);
                        sendMessage("Use ree!setup r6 #R6-Mate-Search-Channel", 5, m, hook);
                    } else {
                        messageSelf.getMentionedChannels().get(0).createWebhook("Ree6-News").queue(w -> Main.sqlWorker.setRainbowWebhook(sender.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("R6 Mate Search channel has been set!", 5, m, hook);
                    }
                } else if (args[0].equalsIgnoreCase("rewards")) {
                    if(args.length != 5) {
                        sendMessage("No Param. error!", 5, m, hook);
                        sendMessage("Use ree!rewards vc/chat add/remove Level @Role", 5, m, hook);
                    } else if (args.length == 5) {
                        if (messageSelf.getMentionedRoles().isEmpty()) {
                            sendMessage("No Role mentioned!", 5, m, hook);
                            sendMessage("Use ree!rewards vc/chat add/remove Level @Role", 5, m, hook);
                        } else {
                            if (args[2].equalsIgnoreCase("add")) {
                                if(args[1].equalsIgnoreCase("vc")) {
                                    Main.sqlWorker.addVoiceLevelReward(m.getGuild().getId(), Integer.parseInt(args[3]), messageSelf.getMentionedRoles().get(0).getId());
                                    sendMessage("VoiceReward has been added!", 5, m, hook);
                                } else if (args[1].equalsIgnoreCase("chat")) {
                                    Main.sqlWorker.addChatLevelReward(m.getGuild().getId(), Integer.parseInt(args[3]), messageSelf.getMentionedRoles().get(0).getId());
                                    sendMessage("ChatReward has been added!", 5, m, hook);
                                } else {
                                    sendMessage("Use ree!rewards vc/chat add/remove Level @Role", 5, m, hook);
                                }
                            } else if (args[2].equalsIgnoreCase("remove")) {
                                if(args[1].equalsIgnoreCase("vc")) {
                                    Main.sqlWorker.removeVoiceLevelReward(m.getGuild().getId(), Integer.parseInt(args[3]));
                                    sendMessage("VoiceReward has been removed!", 5, m, hook);
                                } else if (args[1].equalsIgnoreCase("chat")) {
                                    Main.sqlWorker.removeChatLevelReward(m.getGuild().getId(), Integer.parseInt(args[3]));
                                    sendMessage("ChatReward has been removed!", 5, m, hook);
                                } else {
                                    sendMessage("Use ree!rewards vc/chat add/remove Level @Role", 5, m, hook);
                                }
                            } else {
                                sendMessage("Use ree!rewards vc/chat add/remove Level @Role", 5, m, hook);
                            }
                        }
                    }
                } else {
                    sendMessage("Couldnt find " + args[0] + "!", 5, m, hook);
                    sendMessage("Use ree!setup log/welcome/news/r6/mute/autorole/join/rewards", 5, m, hook);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, m, hook);
                sendMessage("Use ree!setup log/welcome/news/r6/mute/autorole/rewards/join #Log/#Welcome/#Ree6-News/#R6-Mate-Search/@Mute/@Autorole/vc or chat/Your Custom Join Message", 5, m, hook);
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, m, hook);
        }
        deleteMessage(messageSelf);
    }
}
