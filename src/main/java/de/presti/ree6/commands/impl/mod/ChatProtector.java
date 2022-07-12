package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

/**
 * @deprecated This command will be removed soon. In addition to a new upcoming feature, which uses AI Recognition to detect "bad words".
 * @since 1.7.13
 */
@Deprecated(forRemoval = true)
@Command(name = "chatprotector", description = "Manage your Chat Filter.", category = Category.MOD)
public class ChatProtector implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            Main.getInstance().getCommandManager().sendMessage("This Command doesn't support slash commands yet.", commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if(commandEvent.getArguments().length >= 1) {
                if(commandEvent.getArguments().length == 1) {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector remove WORD", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else if (commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                        if(de.presti.ree6.addons.impl.ChatProtector.isChatProtectorSetup(commandEvent.getGuild().getId())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : de.presti.ree6.addons.impl.ChatProtector.getBlacklist(commandEvent.getGuild().getId())) {
                                end.append("\n").append(s);
                            }

                            Main.getInstance().getCommandManager().sendMessage("```" + end + "```", commandEvent.getChannel(), commandEvent.getInteractionHook());
                        } else {
                            Main.getInstance().getCommandManager().sendMessage("Your ChatProtector isn't setuped!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                            Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        }
                    } else {
                        Main.getInstance().getCommandManager().sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    }
                } else {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        if(commandEvent.getArguments().length > 2) {
                            StringBuilder end = new StringBuilder();
                            ArrayList<String> words = new ArrayList<>();
                            for(int i = 2; i < commandEvent.getArguments().length; i++) {
                                words.add(commandEvent.getArguments()[i]);
                                end.append("\n").append(commandEvent.getArguments()[i]);
                            }
                            de.presti.ree6.addons.impl.ChatProtector.blacklist(commandEvent.getGuild().getId(), words);
                            Main.getInstance().getCommandManager().sendMessage("The Wordlist has been added to your ChatProtector!\nYour Wordlist:\n```" + end + "```", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        } else {
                            de.presti.ree6.addons.impl.ChatProtector.blacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                            Main.getInstance().getCommandManager().sendMessage("The Word " + commandEvent.getArguments()[1] + " has been added to your ChatProtector!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        }
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        de.presti.ree6.addons.impl.ChatProtector.removeFromBlacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                        Main.getInstance().getCommandManager().sendMessage("The Word " + commandEvent.getArguments()[1] + " has been removed from your ChatProtector!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        Main.getInstance().getCommandManager().sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    }
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "chatprotector add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[]{ "blacklist", "badword" };
    }
}
