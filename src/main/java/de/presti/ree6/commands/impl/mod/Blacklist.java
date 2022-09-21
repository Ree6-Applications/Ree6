package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Language;
import de.presti.ree6.utils.others.ModerationUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

/**
 * This command is used to manage the Blacklist.
 */
@Command(name = "blacklist", description = "command.description.blacklist", category = Category.MOD)
public class Blacklist implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            Main.getInstance().getCommandManager().sendMessage(Language.getResource(commandEvent.getGuild(), "command.perform.slashNotSupported"), commandEvent.getChannel(), commandEvent.getInteractionHook());
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if(commandEvent.getArguments().length >= 1) {
                if(commandEvent.getArguments().length == 1) {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist remove WORD", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else if (commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                        if(ModerationUtil.shouldModerate(commandEvent.getGuild().getId())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : ModerationUtil.getBlacklist(commandEvent.getGuild().getId())) {
                                end.append("\n").append(s);
                            }

                            Main.getInstance().getCommandManager().sendMessage("```" + end + "```", commandEvent.getChannel(), commandEvent.getInteractionHook());
                        } else {
                            Main.getInstance().getCommandManager().sendMessage("Your blacklist isn't setup!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                            Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist add WORD WORD2 WORD3 AND MORE WORDS", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        }
                    } else {
                        Main.getInstance().getCommandManager().sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
                            ModerationUtil.blacklist(commandEvent.getGuild().getId(), words);
                            Main.getInstance().getCommandManager().sendMessage("The Wordlist has been added to your blacklist!\nYour blacklist:\n```" + end + "```", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        } else {
                            ModerationUtil.blacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                            Main.getInstance().getCommandManager().sendMessage("The Word " + commandEvent.getArguments()[1] + " has been added to your blacklist!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        }
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        ModerationUtil.removeBlacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                        Main.getInstance().getCommandManager().sendMessage("The Word " + commandEvent.getArguments()[1] + " has been removed from your blacklist!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    } else {
                        Main.getInstance().getCommandManager().sendMessage("Couldn't find " + commandEvent.getArguments()[0] + "!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                        Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                    }
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Not enough Arguments!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "blacklist add/remove/list", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You don't have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{ "chatprotector", "badword" };
    }
}
