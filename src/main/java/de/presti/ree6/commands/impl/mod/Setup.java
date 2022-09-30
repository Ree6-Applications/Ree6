package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;

import java.awt.*;
import java.util.ArrayList;
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
