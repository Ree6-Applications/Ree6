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
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("message.default.slashCommandOnly"), 5);
            return;
        }

        if (!commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission"), 5);
            return;
        }

        OptionMapping title = commandEvent.getSlashCommandInteractionEvent().getOption("title");
        OptionMapping content = commandEvent.getSlashCommandInteractionEvent().getOption("content");

        if (title == null || content == null) {
            commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
            return;
        }

        de.presti.ree6.news.Announcement announcement = new de.presti.ree6.news.Announcement(RandomUtils.randomString(16), title.getAsString(), content.getAsString());
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
