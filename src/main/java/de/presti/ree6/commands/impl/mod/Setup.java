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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
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

            if (commandEvent.getSubcommandGroup().isBlank()) {
                if (commandEvent.getSubcommand().isBlank()) {
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
                } else if (commandEvent.getSubcommand().equalsIgnoreCase("autorole")) {
                    // TODO:: update messages.
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(commandEvent.getResource("label.setup"))
                            .setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                            .setColor(Color.cyan)
                            .setDescription(commandEvent.getResource("message.setup.setupMenu"));

                    List<SelectOption> optionList = new ArrayList<>();

                    commandEvent.getGuild().getRoles().forEach(role -> optionList.add(SelectOption.of(role.getName(), role.getId())));

                    SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(commandEvent.getGuild().getIdLong()).forEach(autoRole -> {
                        SelectOption option = optionList.stream().filter(selectOption -> selectOption.getValue().equals(String.valueOf(autoRole.getRoleId()))).findFirst().orElse(null);
                        if (option != null) {
                            optionList.remove(option);
                            optionList.add(option.withDefault(true));
                        }
                    });

                    SelectMenu selectMenu = new StringSelectMenuImpl("setupActionMenu", commandEvent.getResource("message.setup.setupMenuPlaceholder"), 0, 10, false, optionList);

                    if (commandEvent.isSlashCommand()) {
                        commandEvent.getInteractionHook().sendMessageEmbeds(embedBuilder.build())
                                .addActionRow(selectMenu).queue();
                    } else {
                        commandEvent.getChannel().sendMessageEmbeds(embedBuilder.build())
                                .addActionRow(selectMenu).queue();
                    }
                }
            } else {
                switch (commandEvent.getSubcommandGroup()) {
                    case "auditlog" -> {
                        if (commandEvent.getSubcommand().equals("set")) {
                            // TODO:: log set
                        } else {
                            // TODO:: log remove
                        }
                    }
                    case "welcome" -> {
                        if (commandEvent.getSubcommand().equals("set")) {
                            // TODO:: welcome set
                        } else {
                            // TODO:: welcome remove
                        }
                    }
                    case "autorole" -> {
                    }
                    case "tempvoice" -> {
                        if (commandEvent.getSubcommand().equals("set")) {
                            // TODO:: temporal set
                        } else {
                            // TODO:: temporal remove
                        }
                    }
                }
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
        return new CommandDataImpl("setup", "command.description.setup")
                .addSubcommands(new SubcommandData("menu", "Open the Setup Menu."))
                .addSubcommands(new SubcommandData("autorole", "Configure the Autoroles."))
                .addSubcommandGroups(
                        new SubcommandGroupData("auditlog", "Auditlog Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Auditlog channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Welcome Channel.", true).setChannelTypes(ChannelType.TEXT)))
                                .addSubcommands(new SubcommandData("remove", "Remove the Auditlog channel.")),
                        new SubcommandGroupData("welcome", "Welcome Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Welcome channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Auditlog Channel.", true).setChannelTypes(ChannelType.TEXT)))
                                .addSubcommands(new SubcommandData("remove", "Remove the Welcome channel.")),
                        new SubcommandGroupData("tempvoice", "Temporal Voice Setup")
                                .addSubcommands(new SubcommandData("set", "Set a Temporal Voice Channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Temporal Voice Channel.", true).setChannelTypes(ChannelType.VOICE)))
                                .addSubcommands(new SubcommandData("remove", "Remove a Temporal Voice Channel.")),
                        new SubcommandGroupData("statistics", "Statistics Setup")
                                .addSubcommands(new SubcommandData("create", "Create the Statistics channel."))
                                .addSubcommands(new SubcommandData("remove", "Remove the Statistics channel.")),
                        new SubcommandGroupData("tickets", "Ticket System Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Ticket System channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Ticket Channel.", true).setChannelTypes(ChannelType.TEXT)))
                                .addSubcommands(new SubcommandData("remove", "Remove the Ticket System channel.")),
                        new SubcommandGroupData("rewards", "Rewards Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Rewards value."))
                                .addSubcommands(new SubcommandData("reset", "Reset the Rewards value."))
                );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
