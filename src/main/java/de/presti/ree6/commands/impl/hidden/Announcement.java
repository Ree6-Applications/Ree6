package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.news.AnnouncementManager;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Arrays;

// TODO:: add a way to remove and list announcements. Missing Slash Command support.

/**
 * A command to create an announcement.
 */
@Command(name = "announcement", description = "command.description.announcement", category = Category.HIDDEN)
public class Announcement implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission"), 5);
            return;
        }

        if (commandEvent.getArguments().length < 2) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (String s : Arrays.stream(commandEvent.getArguments()).skip(1).toArray(String[]::new)) {
            stringBuilder.append(s).append(" ");
        }

        String title = commandEvent.getArguments()[0].replace("%l%", " ");

        de.presti.ree6.news.Announcement announcement =
                new de.presti.ree6.news.Announcement(RandomUtils.randomString(16), title,
                        stringBuilder.toString());

        AnnouncementManager.addAnnouncement(announcement);

        commandEvent.reply(commandEvent.getResource("message.announcement.added"), 5);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("announcement", LanguageService.getDefault("command.description.announcement"))
                .addOptions(new OptionData(OptionType.STRING, "title", "The title of the announcement", true),
                        new OptionData(OptionType.STRING, "content", "The content of the announcement", true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
