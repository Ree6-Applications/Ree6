package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.news.AnnouncementManager;
import de.presti.ree6.utils.data.Data;
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

        if (!commandEvent.getMember().getUser().getId().equalsIgnoreCase(Data.getBotOwner())) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", "BE DEVELOPER"), 5);
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        OptionMapping title = commandEvent.getOption("title");
        OptionMapping content = commandEvent.getOption("content");
        OptionMapping toDeleteId = commandEvent.getOption("id");

        if (title != null && content != null) {
            de.presti.ree6.news.Announcement announcement =
                    new de.presti.ree6.news.Announcement(RandomUtils.randomString(16, false), title.getAsString(),
                            content.getAsString());

            AnnouncementManager.addAnnouncement(announcement);

            commandEvent.reply(commandEvent.getResource("message.announcement.added"), 5);
        } else if (toDeleteId != null) {
            AnnouncementManager.removeAnnouncement(toDeleteId.getAsString());
            commandEvent.reply(commandEvent.getResource("message.announcement.removed"), 5);
        } else {
            commandEvent.reply(commandEvent.getResource("message.announcement.list", String.join("\n",
                    AnnouncementManager.getAnnouncementList().stream().map(c -> c.id() + " -> " + c.title()).toArray(String[]::new))));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("announcement", LanguageService.getDefault("command.description.announcement"))
                .addOptions(new OptionData(OptionType.STRING, "title", "The title of the announcement.", false),
                        new OptionData(OptionType.STRING, "content", "The content of the announcement.", false),
                        new OptionData(OptionType.STRING, "id", "The to delete announcement id.", false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
