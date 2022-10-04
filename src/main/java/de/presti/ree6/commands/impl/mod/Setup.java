package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A command to set Ree6 up.
 */
@Command(name = "setup", description = "Setup specific default operations for Ree6!", category = Category.MOD)
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

                    commandEvent.reply("You need to attach an image to this command!");
                    return;
                } else {
                    try (Message.Attachment attachment = commandEvent.getMessage().getAttachments().stream().filter(Message.Attachment::isImage).findFirst().orElse(null)) {
                        if (attachment != null) {
                            if (attachment.getSize() > 1024 * 1024 * 20) {
                                commandEvent.reply("The image is too big! It needs to be smaller than 20MB!");
                                return;
                            }

                            try (InputStream inputStream = attachment.getProxy().download(1920, 1080).get()) {
                                byte[] imageArray = inputStream.readAllBytes();

                                Main.getInstance().getSqlConnector().getSqlWorker()
                                        .setSetting(new Setting(commandEvent.getGuild().getId(), "message_join_image", Base64.getEncoder().encodeToString(imageArray)));
                                commandEvent.reply("Successfully set the join image!");
                            } catch (Exception e) {
                                commandEvent.reply("Couldn't convert the Image!");
                                Main.getInstance().getLogger().error("Couldn't convert the Image!", e);
                            }
                        }
                    }
                }
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Setup Menu")
                    .setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl())
                    .setColor(Color.cyan)
                    .setDescription("Which configuration do you want to check out?");

            List<SelectOption> optionList = new ArrayList<>();
            optionList.add(SelectOption.of("Audit-Logging", "log"));
            optionList.add(SelectOption.of("Welcome-channel", "welcome"));
            optionList.add(SelectOption.of("Autorole", "autorole"));
            optionList.add(SelectOption.of("Temporal-Voice", "tempvoice"));
            optionList.add(SelectOption.of("Statistics", "statistics"));

            SelectMenu selectMenu = new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList);

            if (commandEvent.isSlashCommand()) {
                commandEvent.getInteractionHook().sendMessageEmbeds(embedBuilder.build())
                        .addActionRow(selectMenu).queue();
            } else {
                commandEvent.getChannel().sendMessageEmbeds(embedBuilder.build())
                        .addActionRow(selectMenu).queue();
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
