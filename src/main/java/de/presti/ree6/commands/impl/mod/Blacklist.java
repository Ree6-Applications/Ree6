package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.ModerationUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

/**
 * This command is used to manage the Blacklist.
 */
@Command(name = "blacklist", description = "command.description.blacklist", category = Category.MOD)
public class Blacklist implements ICommand {

    // TODO:: add or switch to slash commands.

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.slashNotSupported"));
            return;
        }

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if(commandEvent.getArguments().length >= 1) {
                if(commandEvent.getArguments().length == 1) {
                    if(commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist add [WORD...]"), 5);
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist remove WORD"), 5);
                    } else if (commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                        if(ModerationUtil.shouldModerate(commandEvent.getGuild().getId())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : ModerationUtil.getBlacklist(commandEvent.getGuild().getId())) {
                                end.append("\n").append(s);
                            }

                            commandEvent.reply("```" + end + "```");
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.blacklist.setupNeeded"));
                            commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist add [WORD...]"), 5);
                        }
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.blacklist.notFound", commandEvent.getArguments()[0]), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist add/remove/list"),5);
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
                            commandEvent.reply(commandEvent.getResource("message.blacklist.addedList","```" + end + "```"), 5);
                        } else {
                            ModerationUtil.blacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                            commandEvent.reply(commandEvent.getResource("message.blacklist.added", commandEvent.getArguments()[1]), 5);
                        }
                    } else if(commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                        ModerationUtil.removeBlacklist(commandEvent.getGuild().getId(), commandEvent.getArguments()[1]);
                        commandEvent.reply(commandEvent.getResource("message.blacklist.removed", commandEvent.getArguments()[1]), 5);
                    } else {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                        commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist add/remove/list"),5);
                    }
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                commandEvent.reply(commandEvent.getResource("message.default.usage","blacklist add/remove/list"),5);
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name()), 5);
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
