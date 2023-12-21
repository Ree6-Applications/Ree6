package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.ModerationUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

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

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            if (commandEvent.isSlashCommand()) {

                OptionMapping wordOption = commandEvent.getOption("word");

                switch (commandEvent.getSubcommand()) {
                    case "add" -> {
                        String word = wordOption.getAsString();
                        if (word.contains(" ")) {
                            StringBuilder end = new StringBuilder();
                            ArrayList<String> words = new ArrayList<>();

                            for (String wordEntry : word.split("\\s+")) {
                                words.add(wordEntry);
                                end.append("\n").append(wordEntry);
                            }
                            ModerationUtil.blacklist(commandEvent.getGuild().getIdLong(), words);
                            commandEvent.reply(commandEvent.getResource("message.blacklist.addedList", "```" + end + "```"), 5);
                        } else {
                            ModerationUtil.blacklist(commandEvent.getGuild().getIdLong(), word);
                            commandEvent.reply(commandEvent.getResource("message.blacklist.added", word), 5);
                        }
                    }

                    case "remove" -> {
                        String word = wordOption.getAsString();

                        ModerationUtil.removeBlacklist(commandEvent.getGuild().getIdLong(), word);
                        commandEvent.reply(commandEvent.getResource("message.blacklist.removed", word), 5);
                    }

                    default -> {
                        if (ModerationUtil.shouldModerate(commandEvent.getGuild().getIdLong())) {
                            StringBuilder end = new StringBuilder();

                            for (String s : ModerationUtil.getBlacklist(commandEvent.getGuild().getIdLong())) {
                                end.append("\n").append(s);
                            }

                            commandEvent.reply("```" + end + "```");
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.blacklist.setupNeeded"));
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add [WORD...]"), 5);
                        }
                    }
                }

            } else {
                if (commandEvent.getArguments().length >= 1) {
                    if (commandEvent.getArguments().length == 1) {
                        if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add [WORD...]"), 5);
                        } else if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist remove WORD"), 5);
                        } else if (commandEvent.getArguments()[0].equalsIgnoreCase("list")) {
                            if (ModerationUtil.shouldModerate(commandEvent.getGuild().getIdLong())) {
                                StringBuilder end = new StringBuilder();

                                for (String s : ModerationUtil.getBlacklist(commandEvent.getGuild().getIdLong())) {
                                    end.append("\n").append(s);
                                }

                                commandEvent.reply("```" + end + "```");
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.blacklist.setupNeeded"));
                                commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add [WORD...]"), 5);
                            }
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.blacklist.notFound", commandEvent.getArguments()[0]), 5);
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add/remove/list"), 5);
                        }
                    } else {
                        if (commandEvent.getArguments()[0].equalsIgnoreCase("add")) {
                            if (commandEvent.getArguments().length > 2) {
                                StringBuilder end = new StringBuilder();
                                ArrayList<String> words = new ArrayList<>();
                                for (int i = 2; i < commandEvent.getArguments().length; i++) {
                                    words.add(commandEvent.getArguments()[i]);
                                    end.append("\n").append(commandEvent.getArguments()[i]);
                                }
                                ModerationUtil.blacklist(commandEvent.getGuild().getIdLong(), words);
                                commandEvent.reply(commandEvent.getResource("message.blacklist.addedList", "```" + end + "```"), 5);
                            } else {
                                ModerationUtil.blacklist(commandEvent.getGuild().getIdLong(), commandEvent.getArguments()[1]);
                                commandEvent.reply(commandEvent.getResource("message.blacklist.added", commandEvent.getArguments()[1]), 5);
                            }
                        } else if (commandEvent.getArguments()[0].equalsIgnoreCase("remove")) {
                            ModerationUtil.removeBlacklist(commandEvent.getGuild().getIdLong(), commandEvent.getArguments()[1]);
                            commandEvent.reply(commandEvent.getResource("message.blacklist.removed", commandEvent.getArguments()[1]), 5);
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                            commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add/remove/list"), 5);
                        }
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage", "blacklist add/remove/list"), 5);
                }
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
        return new CommandDataImpl("blacklist", "command.description.blacklist")
                .addSubcommands(
                        new SubcommandData("add", "Add a word to the blacklist")
                                .addOption(OptionType.STRING, "word", "A single word or multiple words split by a space", true),
                        new SubcommandData("remove", "Remove a word from the blacklist")
                                .addOption(OptionType.STRING, "word", "The word to remove", true),
                        new SubcommandData("list", "List all words on the blacklist"));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"chatprotector", "badword"};
    }
}
