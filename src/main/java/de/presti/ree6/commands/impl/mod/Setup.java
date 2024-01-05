package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A command to set Ree6 up.
 */
@Command(name = "setup", description = "command.description.setup", category = Category.MOD)
public class Setup implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {

            if (!commandEvent.isSlashCommand() &&
                    commandEvent.getArguments().length == 1 &&
                    commandEvent.getArguments()[0].equalsIgnoreCase("joinimage") &&
                    commandEvent.getMessage() != null) {

                if (commandEvent.getMessage().getAttachments().isEmpty() ||
                        commandEvent.getMessage().getAttachments().stream().noneMatch(Message.Attachment::isImage)) {
                    commandEvent.reply(commandEvent.getResource("message.default.image.needed"), 5);
                } else {
                    try (Message.Attachment attachment = commandEvent.getMessage().getAttachments().stream().filter(Message.Attachment::isImage).findFirst().orElse(null)) {
                        if (attachment != null) {
                            if (attachment.getSize() > 1024 * 1024 * 20) {
                                commandEvent.reply(commandEvent.getResource("message.default.image.tooBigMax", "20"), 5);
                            } else {
                                try (InputStream inputStream = attachment.getProxy().download(1920, 1080).get()) {
                                    byte[] imageArray = inputStream.readAllBytes();

                                    SQLSession.getSqlConnector().getSqlWorker()
                                            .setSetting(new Setting(commandEvent.getGuild().getIdLong(), "message_join_image", "Welcome Image", Base64.getEncoder().encodeToString(imageArray)));
                                    commandEvent.reply(commandEvent.getResource("message.setup.successImage"));
                                } catch (Exception e) {
                                    commandEvent.reply(commandEvent.getResource("command.perform.error"));
                                    log.error("Couldn't convert the Image!", e);
                                }
                            }
                        }
                    }
                }
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(commandEvent.getResource("label.setup"))
                    .setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                    .setColor(Color.cyan)
                    .setDescription(commandEvent.getResource("message.setup.setupMenu"));

            List<SelectOption> optionList = new ArrayList<>();
            optionList.add(SelectOption.of(commandEvent.getResource("label.language"), "lang"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.auditLog"), "log"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.welcomeChannel"), "welcome"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.autoRole"), "autorole"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.temporalVoice"), "tempvoice"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.statistics"), "statistics"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.ticketSystem"), "tickets"));
            optionList.add(SelectOption.of(commandEvent.getResource("label.rewards"), "rewards"));

            SelectMenu selectMenu = new StringSelectMenuImpl("setupActionMenu", commandEvent.getResource("message.setup.setupMenuPlaceholder"), 1, 1, false, optionList);

            if (commandEvent.isSlashCommand()) {
                commandEvent.getInteractionHook().sendMessageEmbeds(embedBuilder.build())
                        .addActionRow(selectMenu).queue();
            } else {
                commandEvent.getChannel().sendMessageEmbeds(embedBuilder.build())
                        .addActionRow(selectMenu).queue();
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name() + "/" + Permission.MANAGE_SERVER.name()));
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
        return new String[0];
    }
}
