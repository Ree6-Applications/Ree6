package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;

public class Setup extends Command {

    public Setup() {
        super("setup", "Setup the Welcome and Log Channel!", Category.MOD);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            sendMessage("This Command doesn't support slash commands yet.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        // TODO rework.

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (commandEvent.getArguments().length >= 1) {
                if (commandEvent.getArguments()[0].equalsIgnoreCase("log")) {
                    if (commandEvent.getMessage().getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup log #Log-Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-Log").queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("Log channel has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("welcome")) {
                    if (commandEvent.getMessage().getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup welcome #Welcome-Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-Welcome").queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("Welcome channel has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("mute")) {
                    if (commandEvent.getMessage().getMentionedRoles().isEmpty()) {
                        sendMessage("No Role mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup mute @Muterole", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        Main.getInstance().getSqlConnector().getSqlWorker().setMuteRole(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentionedRoles().get(0).getId());
                        sendMessage("Mute Role has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("autorole")) {
                    if (commandEvent.getArguments().length == 3) {
                        if (commandEvent.getMessage().getMentionedRoles().isEmpty()) {
                            sendMessage("No Role mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup autorole add/remove @role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        } else {
                            if (commandEvent.getArguments()[1].equalsIgnoreCase("add")) {
                                Main.getInstance().getSqlConnector().getSqlWorker().addAutoRole(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentionedRoles().get(0).getId());
                                sendMessage("Autorole has been added!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            } else if (commandEvent.getArguments()[1].equalsIgnoreCase("remove")) {
                                Main.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentionedRoles().get(0).getId());
                                sendMessage("Autorole has been removed!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            } else {
                                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup autorole add/remove @role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            }
                        }
                    } else {
                        sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup autorole add/remove @role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("news")) {
                    if (commandEvent.getMessage().getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup news #Ree6-News", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-News").queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().setNewsWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("News channel has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("join")) {
                    if (commandEvent.getArguments().length == 1) {
                        sendMessage("No Message given!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "join Your Join Message", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Usable Syntaxes: %user_name%, %guild_name%, %user_mention%", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        StringBuilder message = new StringBuilder();

                        for (int i = 1; i < commandEvent.getArguments().length; i++) {
                            message.append(commandEvent.getArguments()[i]);
                            message.append(" ");
                        }

                        if (message.length() >= 250) {
                            sendMessage("Your Welcome Message cant be longer than 250", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            return;
                        }

                        Main.getInstance().getSqlConnector().getSqlWorker().setMessage(commandEvent.getGuild().getId(), message.toString());

                        sendMessage("Join Message has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("r6")) {
                    if (commandEvent.getMessage().getMentionedChannels().isEmpty()) {
                        sendMessage("No Channel mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup r6 #R6-Mate-Search-Channel", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        commandEvent.getMessage().getMentionedChannels().get(0).createWebhook("Ree6-MateSearch").queue(w -> Main.getInstance().getSqlConnector().getSqlWorker().setRainbowWebhook(commandEvent.getGuild().getId(), w.getId(), w.getToken()));
                        sendMessage("R6 Mate Search channel has been set!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    }
                } else if (commandEvent.getArguments()[0].equalsIgnoreCase("rewards")) {
                    if(commandEvent.getArguments().length != 5) {
                        sendMessage("No Param. error!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "rewards vc/chat add/remove Level @Role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    } else {
                        if (commandEvent.getMessage().getMentionedRoles().isEmpty()) {
                            sendMessage("No Role mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "rewards vc/chat add/remove Level @Role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        } else {
                            if (commandEvent.getArguments()[2].equalsIgnoreCase("add")) {
                                if(commandEvent.getArguments()[1].equalsIgnoreCase("vc")) {
                                    Main.getInstance().getSqlConnector().getSqlWorker().addVoiceLevelReward(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentionedRoles().get(0).getId(), Integer.parseInt(commandEvent.getArguments()[3]));
                                    sendMessage("VoiceReward has been added!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                } else if (commandEvent.getArguments()[1].equalsIgnoreCase("chat")) {
                                    Main.getInstance().getSqlConnector().getSqlWorker().addChatLevelReward(commandEvent.getGuild().getId(), commandEvent.getMessage().getMentionedRoles().get(0).getId(), Integer.parseInt(commandEvent.getArguments()[3]));
                                    sendMessage("ChatReward has been added!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                } else {
                                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "rewards vc/chat add/remove Level @Role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                }
                            } else if (commandEvent.getArguments()[2].equalsIgnoreCase("remove")) {
                                if(commandEvent.getArguments()[1].equalsIgnoreCase("vc")) {
                                    Main.getInstance().getSqlConnector().getSqlWorker().removeVoiceLevelReward(commandEvent.getGuild().getId(), Integer.parseInt(commandEvent.getArguments()[3]));
                                    sendMessage("VoiceReward has been removed!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                } else if (commandEvent.getArguments()[1].equalsIgnoreCase("chat")) {
                                    Main.getInstance().getSqlConnector().getSqlWorker().removeChatLevelReward(commandEvent.getGuild().getId(), Integer.parseInt(commandEvent.getArguments()[3]));
                                    sendMessage("ChatReward has been removed!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                } else {
                                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "rewards vc/chat add/remove Level @Role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                                }
                            } else {
                                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "rewards vc/chat add/remove Level @Role", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                            }
                        }
                    }
                } else {
                    sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup log/welcome/news/r6/mute/autorole/join/rewards", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            } else {
                sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup log/welcome/news/r6/mute/autorole/rewards/join #Log/#Welcome/#Ree6-News/#R6-Mate-Search/@Mute/@Autorole/vc or chat/Your Custom Join Message", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
