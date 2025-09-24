package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.util.SettingsManager;
import de.presti.ree6.utils.others.GuildUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
                if (commandEvent.getSubcommand().isBlank() || commandEvent.getSubcommand().equalsIgnoreCase("menu")) {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle(commandEvent.getResource("label.setup"))
                            .setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl())
                            .setColor(BotConfig.getMainColor())
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

                    StringSelectMenu selectMenu = StringSelectMenu.create("setupActionMenu")
                            .addOptions(optionList)
                            .setPlaceholder(commandEvent.getResource("message.setup.setupMenuPlaceholder"))
                            .setMinValues(1)
                            .setMaxValues(1)
                            .build();

                    if (commandEvent.isSlashCommand()) {
                        commandEvent.getInteractionHook().sendMessageEmbeds(embedBuilder.build())
                                .setComponents(ActionRow.of(selectMenu)).queue();
                    } else {
                        commandEvent.getChannel().sendMessageEmbeds(embedBuilder.build())
                                .setComponents(ActionRow.of(selectMenu)).queue();
                    }
                } else if (commandEvent.getSubcommand().equalsIgnoreCase("autorole")) {
                    createAutoRoleSetupSelectMenu(commandEvent.getGuild(), commandEvent.getInteractionHook()).subscribe(menu -> {
                        MessageEmbed embed = createAutoRoleSetupMessage(commandEvent.getGuild(), commandEvent.getInteractionHook()).build();
                        Button webinterface = Button.link(BotConfig.getWebinterface(), "Webinterface");

                        if (commandEvent.isSlashCommand()) {
                            commandEvent.getInteractionHook().sendMessageEmbeds(embed)
                                    .setComponents(ActionRow.of(menu), ActionRow.of(webinterface)).queue();
                        } else {
                            commandEvent.getChannel().sendMessageEmbeds(embed)
                                    .setComponents(ActionRow.of(menu), ActionRow.of(webinterface)).queue();
                        }
                    });
                }
            } else {
                OptionMapping optionMapping = commandEvent.getOption("channel");

                if (optionMapping == null) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"), 5);
                    return;
                }

                GuildChannelUnion guildChannelUnion = optionMapping.getAsChannel();

                switch (commandEvent.getSubcommandGroup()) {
                    case "auditlog" -> {
                        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_WEBHOOKS.name()));
                            return;
                        }

                        if (commandEvent.getSubcommand().equals("set")) {
                            if (guildChannelUnion.getType() == ChannelType.TEXT) {
                                guildChannelUnion.asTextChannel().createWebhook(BotConfig.getBotName() + "-Logs").queue(webhook -> SQLSession.getSqlConnector().getSqlWorker().isLogSetup(commandEvent.getGuild().getIdLong()).subscribe(aBoolean -> {
                                    if (aBoolean) {
                                        SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(commandEvent.getGuild().getIdLong()).subscribe(webhookEntity -> webhookEntity.ifPresent(entity -> WebhookUtil.deleteWebhook(commandEvent.getGuild().getIdLong(), entity)));
                                    }

                                    SQLSession.getSqlConnector().getSqlWorker().setLogWebhook(commandEvent.getGuild().getIdLong(), guildChannelUnion.getIdLong(), webhook.getIdLong(), webhook.getToken());
                                    commandEvent.reply(commandEvent.getResource("message.auditLog.setupSuccess"));
                                }));
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.invalidOptionChannel"));
                            }
                        } else {
                            SQLSession.getSqlConnector().getSqlWorker().isLogSetup(commandEvent.getGuild().getIdLong()).subscribe(aBoolean -> {
                                if (aBoolean) {
                                    SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(commandEvent.getGuild().getIdLong()).subscribe(webhookEntity -> {
                                        webhookEntity.ifPresent(entity -> WebhookUtil.deleteWebhook(commandEvent.getGuild().getIdLong(), entity));
                                        commandEvent.reply(commandEvent.getResource("message.auditLog.deleted"));
                                    });
                                } else {
                                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                                }
                            });
                        }
                    }
                    case "welcome" -> {
                        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_WEBHOOKS.name()));
                            return;
                        }

                        if (commandEvent.getSubcommand().equals("set")) {
                            if (guildChannelUnion.getType() == ChannelType.TEXT) {
                                guildChannelUnion.asTextChannel().createWebhook(BotConfig.getBotName() + "-Welcome").queue(webhook -> SQLSession.getSqlConnector().getSqlWorker().isWelcomeSetup(commandEvent.getGuild().getIdLong()).subscribe(aBoolean -> {
                                    if (aBoolean) {
                                        SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(commandEvent.getGuild().getIdLong()).subscribe(webhookEntity -> webhookEntity.ifPresent(entity -> WebhookUtil.deleteWebhook(commandEvent.getGuild().getIdLong(), entity)));
                                    }
                                    SQLSession.getSqlConnector().getSqlWorker().setWelcomeWebhook(commandEvent.getGuild().getIdLong(), guildChannelUnion.getIdLong(), webhook.getIdLong(), webhook.getToken());
                                    commandEvent.reply(commandEvent.getResource("message.welcome.setupSuccess"));
                                }));
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.invalidOptionChannel"));
                            }
                        } else {
                            SQLSession.getSqlConnector().getSqlWorker().isWelcomeSetup(commandEvent.getGuild().getIdLong()).subscribe(aBoolean -> {
                                if (aBoolean) {
                                    SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(commandEvent.getGuild().getIdLong()).subscribe(webhookEntity ->
                                            webhookEntity.ifPresent(entity -> WebhookUtil.deleteWebhook(commandEvent.getGuild().getIdLong(), entity)));
                                    commandEvent.reply(commandEvent.getResource("message.welcome.deleted"));
                                }
                                commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                            });
                        }
                    }
                    case "tempvoice" -> {
                        if (commandEvent.getSubcommand().equals("set")) {
                            if (guildChannelUnion.getType() == ChannelType.VOICE) {
                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(new TemporalVoicechannel(commandEvent.getGuild().getIdLong(), guildChannelUnion.getIdLong())).subscribe(save -> commandEvent.reply(commandEvent.getResource("message.temporalVoice.setupSuccess")));
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.invalidOptionChannel"));
                            }
                        } else {
                            SQLSession.getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", commandEvent.getGuild().getId())).subscribe(temporalVoicechannel -> {
                                if (temporalVoicechannel.isPresent()) {
                                    SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel.get()).block();
                                    commandEvent.reply(commandEvent.getResource("message.temporalVoice.deleted"));
                                } else {
                                    commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                                }
                            });
                        }
                    }
                    case "rewards" -> {
                        if (commandEvent.getSubcommand().equals("set")) {
                            OptionMapping blackJackWin = commandEvent.getOption("blackJackWin");
                            OptionMapping musicQuizWin = commandEvent.getOption("musicQuizWin");
                            OptionMapping musicQuizFeature = commandEvent.getOption("musicQuizFeature");
                            OptionMapping musicQuizArtist = commandEvent.getOption("musicQuizArtist");
                            OptionMapping musicQuizTitle = commandEvent.getOption("musicQuizTitle");

                            if (blackJackWin != null) {
                                SQLSession.getSqlConnector().getSqlWorker().setSetting(new Setting(commandEvent.getGuild().getIdLong(), "configuration_rewards_blackJackWin", "BlackJack Win", blackJackWin.getAsDouble()));
                            }

                            if (musicQuizWin != null) {
                                SQLSession.getSqlConnector().getSqlWorker().setSetting(new Setting(commandEvent.getGuild().getIdLong(), "configuration_rewards_musicQuizWin", "MusicQuiz Win", musicQuizWin.getAsDouble()));
                            }

                            if (musicQuizFeature != null) {
                                SQLSession.getSqlConnector().getSqlWorker().setSetting(new Setting(commandEvent.getGuild().getIdLong(), "configuration_rewards_musicQuizFeature", "MusicQuiz Feature", musicQuizFeature.getAsDouble()));
                            }

                            if (musicQuizArtist != null) {
                                SQLSession.getSqlConnector().getSqlWorker().setSetting(new Setting(commandEvent.getGuild().getIdLong(), "configuration_rewards_musicQuizArtist", "MusicQuiz Artist", musicQuizArtist.getAsDouble()));
                            }

                            if (musicQuizTitle != null) {
                                SQLSession.getSqlConnector().getSqlWorker().setSetting(new Setting(commandEvent.getGuild().getIdLong(), "configuration_rewards_musicQuizTitle", "MusicQuiz Title", musicQuizTitle.getAsDouble()));
                            }

                            commandEvent.reply(commandEvent.getResource("message.rewards.success"));
                        } else {
                            for (Setting setting : SettingsManager.getSettings()) {
                                if (!setting.getName().startsWith("configuration_rewards_")) continue;

                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(new Setting(commandEvent.getGuild().getIdLong(), setting.getName(), setting.getDisplayName(), setting.getValue())).block();
                            }

                            commandEvent.reply(commandEvent.getResource("message.rewards.success"));
                        }
                    }
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.ADMINISTRATOR.name() + "/" + Permission.MANAGE_SERVER.name()));
        }

        commandEvent.delete();
    }

    public static EmbedBuilder createAutoRoleSetupMessage(Guild guild, InteractionHook interactionHook) {
        boolean hasRoles = !GuildUtil.getManageableRoles(guild).isEmpty();

        return new EmbedBuilder()
                .setTitle(LanguageService.getByGuildOrInteractionHook(guild, interactionHook, "label.setup").block())
                .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl())
                .setColor(hasRoles ? BotConfig.getMainColor() : Color.RED)
                .setDescription(LanguageService.getByGuildOrInteractionHook(guild, interactionHook, hasRoles ? "message.autoRole.setupDescription" : "message.default.needPermission", (hasRoles ? null : Permission.MANAGE_ROLES.name())).block());
    }

    public static Mono<SelectMenu> createAutoRoleSetupSelectMenu(Guild guild, InteractionHook interactionHook) {
        List<SelectOption> optionList = new ArrayList<>();

        for (Role role : GuildUtil.getManageableRoles(guild)) {
            optionList.add(SelectOption.of(role.getName(), role.getId()));
        }

        return SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guild.getIdLong()).map(list -> {
            list.forEach(autoRole -> {
                SelectOption option = optionList.stream().filter(selectOption -> selectOption.getValue().equals(String.valueOf(autoRole.getRoleId()))).findFirst().orElse(null);
                if (option != null) {
                    optionList.remove(option);
                    optionList.add(option.withDefault(true));
                }
            });

            return StringSelectMenu.create("setupAutoRole")
                    .addOptions(optionList)
                    .setPlaceholder(LanguageService.getByGuildOrInteractionHook(guild, interactionHook,
                            "message.autoRole.setupPlaceholder").block())
                    .setMinValues(0)
                    .setMaxValues(Math.min(10, Math.max(1, optionList.size())))
                    .setDisabled(optionList.isEmpty())
                    .build();
        });
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
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Welcome Channel.", true)
                                                .setChannelTypes(ChannelType.TEXT)))
                                .addSubcommands(new SubcommandData("remove", "Remove the Auditlog channel.")),
                        new SubcommandGroupData("welcome", "Welcome Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Welcome channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Auditlog Channel.", true)
                                                .setChannelTypes(ChannelType.TEXT)))
                                .addSubcommands(new SubcommandData("remove", "Remove the Welcome channel.")),
                        new SubcommandGroupData("tempvoice", "Temporal Voice Setup")
                                .addSubcommands(new SubcommandData("set", "Set a Temporal Voice Channel.")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The Temporal Voice Channel.", true)
                                                .setChannelTypes(ChannelType.VOICE)))
                                .addSubcommands(new SubcommandData("remove", "Remove a Temporal Voice Channel.")),
                        // TODO:: think about a better way to implement it.
                        /*new SubcommandGroupData("statistics", "Statistics Setup")
                                .addSubcommands(new SubcommandData("create", "Create the Statistics channel."))
                                .addSubcommands(new SubcommandData("remove", "Remove the Statistics channel.")),*/
                        new SubcommandGroupData("rewards", "Rewards Setup")
                                .addSubcommands(new SubcommandData("set", "Set the Rewards value.")
                                        .addOptions(
                                                new OptionData(OptionType.NUMBER, "blackjackwin", "The amount of money the user gets for a BlackJack Win.", false),
                                                new OptionData(OptionType.NUMBER, "musicquizwin", "The amount of money the user gets for a MusicQuiz Win.", false),
                                                new OptionData(OptionType.NUMBER, "musicquizfeature", "The amount of money the user gets for the correct MusicQuiz Feature guess.", false),
                                                new OptionData(OptionType.NUMBER, "musicquizartist", "The amount of money the user gets for the correct MusicQuiz Artist guess.", false),
                                                new OptionData(OptionType.NUMBER, "musicquiztitle", "The amount of money the user gets for the correct MusicQuiz Title guess.", false)
                                        ))
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
