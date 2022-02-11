package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Setup extends Command {

    public Setup() {
        super("setup", "Setup the Welcome and Log Channel!", Category.MOD);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR)) {

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Setup Menu")
                    .setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl())
                    .setColor(Color.cyan)
                    .setDescription("Which configuration do you want to check out?");

            List<SelectOption> optionList = new ArrayList<>();
            optionList.add(SelectOption.of("Audit-Logging", "log"));
            optionList.add(SelectOption.of("Welcome-channel", "welcome"));
            optionList.add(SelectOption.of("News-channel", "news"));
            optionList.add(SelectOption.of("Mute role", "mute"));
            optionList.add(SelectOption.of("Autorole", "autrorole"));

            SelectMenu selectMenu = new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList);

            if (commandEvent.isSlashCommand()) {
                commandEvent.getInteractionHook().sendMessageEmbeds(embedBuilder.build())
                        .addActionRow(selectMenu).queue();
            } else {
                commandEvent.getTextChannel().sendMessageEmbeds(embedBuilder.build())
                        .setActionRows(ActionRow.of(selectMenu)).queue();
            }
        } else {
            sendMessage("You dont have the Permission for this Command!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
