package de.presti.ree6.commands.impl.community;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.StreamAction;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.actions.streamtools.container.StreamActionContainer;
import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A command used to create and manage StreamActions.
 */
@Command(name = "stream-action", description = "command.description.stream-action", category = Category.COMMUNITY)
public class StreamActionCommand implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        OptionMapping name = commandEvent.getOption("name");

        String subCommandGroup = commandEvent.getSubcommandGroup();
        String subCommand = commandEvent.getSubcommand();

        switch (subCommandGroup) {
            case "manage" -> {
                StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                        .getEntity(new StreamAction(), "FROM StreamAction WHERE guildAndName.name = :name AND guildAndName.guildId = :gid",
                                Map.of("name", name.getAsString(), "gid", commandEvent.getGuild().getIdLong()));

                if (streamAction != null) {
                    switch (subCommand) {
                        case "create" -> {
                            OptionMapping action = commandEvent.getOption("action");

                            String[] values = action.getAsString().split("\\s+");

                            JsonObject jsonObject = new JsonObject();
                            String actionName = values[0];
                            jsonObject.addProperty("action", actionName);

                            values = Arrays.stream(values).skip(1).toArray(String[]::new);

                            jsonObject.addProperty("value", String.join(" ", values));

                            if (streamAction.getActions() == null || !streamAction.getActions().isJsonArray()) {
                                streamAction.setActions(new JsonArray());
                            }

                            streamAction.getActions().getAsJsonArray().add(jsonObject);

                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);

                            commandEvent.reply(commandEvent.getResource("message.stream-action.addedLine", actionName));
                        }

                        case "delete" -> {
                            try {
                                OptionMapping line = commandEvent.getOption("line");
                                int value = line.getAsInt();
                                if (streamAction.getActions().getAsJsonArray().size() >= value && value > 0) {
                                    streamAction.getActions().getAsJsonArray().remove(value - 1);
                                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                                    commandEvent.reply(commandEvent.getResource("message.stream-action.deletedLine", "" + value));
                                } else {
                                    commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                                }
                            } catch (Exception exception) {
                                commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            }
                        }

                        case "list" -> {
                            StreamActionContainer streamActionContainer = new StreamActionContainer(streamAction);

                            StringBuilder stringBuilder = new StringBuilder();
                            streamActionContainer.getActions()
                                    .forEach(actionRun ->
                                            stringBuilder.append(actionRun.getAction().getClass().getAnnotation(ActionInfo.class).name())
                                                    .append(" -> ")
                                                    .append(String.join(" ", actionRun.getArguments())).append("\n"));

                            commandEvent.reply(commandEvent.getResource("message.stream-action.actionList", stringBuilder.toString()));
                        }

                        case "listener" -> {
                            OptionMapping listener = commandEvent.getOption("listener");
                            String[] values = listener.getAsString().split("\\s+");

                            if (values.length >= 1) {
                                if (values[0].equalsIgnoreCase("redemption")) {
                                    streamAction.setListener(0);
                                } else if (values[0].equalsIgnoreCase("follow")) {
                                    streamAction.setListener(1);
                                } else if (values[0].equalsIgnoreCase("subscribe")) {
                                    streamAction.setListener(2);
                                } else {
                                    commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                                    return;
                                }

                                if (values.length >= 2)
                                    streamAction.setArgument(values[1]);

                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                                if (values.length >= 2) {
                                    commandEvent.reply(commandEvent.getResource("message.stream-action.listenerArgument", values[0], values[1]));
                                } else {
                                    commandEvent.reply(commandEvent.getResource("message.stream-action.listener", values[0]));
                                }
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            }
                        }

                        default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                    }
                } else {
                    commandEvent.reply(commandEvent.getResource("message.stream-action.notFound", name.getAsString()));
                }
            }

            default -> {

                switch (subCommand) {
                    case "create" -> {
                        StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                                .getEntity(new StreamAction(), "FROM StreamAction WHERE guildAndName.actionName = :name AND guildAndName.guildId = :gid",
                                        Map.of("name", name.getAsString(), "gid", commandEvent.getGuild().getIdLong()));

                        if (streamAction == null) {
                            TwitchIntegration twitchIntegration = SQLSession.getSqlConnector().getSqlWorker()
                                    .getEntity(new TwitchIntegration(),"FROM TwitchIntegration WHERE userId = :uid", Map.of("uid", commandEvent.getUser().getIdLong()));
                            if (twitchIntegration != null) {
                                streamAction = new StreamAction();
                                streamAction.setIntegration(twitchIntegration);
                                streamAction.setGuildId(commandEvent.getGuild().getIdLong());
                                streamAction.setName(name.getAsString());

                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                                commandEvent.reply(commandEvent.getResource("message.stream-action.added", name.getAsString()));
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.stream-action.noTwitch", BotConfig.getTwitchAuth()));
                            }
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.stream-action.alreadyExisting", name.getAsString()));
                        }
                    }

                    case "delete" -> {
                        StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                                .getEntity(new StreamAction(), "FROM StreamAction WHERE guildAndName.actionName = :name AND guildAndName.guildId = :gid",
                                        Map.of("name", name.getAsString(), "gid", commandEvent.getGuild().getIdLong()));
                        if (streamAction != null) {
                            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(streamAction);
                            commandEvent.reply(commandEvent.getResource("message.stream-action.deleted", name.getAsString()));
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.stream-action.notFound", name.getAsString()));
                        }
                    }

                    case "list" -> {
                        List<StreamAction> streamActions = SQLSession.getSqlConnector().getSqlWorker()
                                .getEntityList(new StreamAction(), "FROM StreamAction WHERE guildAndName.guildId = :gid",
                                        Map.of("gid", commandEvent.getGuild().getIdLong()));

                        commandEvent.reply(LanguageService.getByEvent(commandEvent, "message.stream-action.list",
                                String.join("\n", streamActions.stream().map(StreamAction::getName).toArray(String[]::new))));
                    }

                    case "points" -> {
                        TwitchIntegration twitchIntegration = SQLSession.getSqlConnector().getSqlWorker()
                                .getEntity(new TwitchIntegration(), "FROM TwitchIntegration WHERE userId = :uid", Map.of("uid", commandEvent.getUser().getIdLong()));
                        if (twitchIntegration != null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            Main.getInstance().getNotifier().getTwitchClient().getHelix()
                                    .getCustomRewards(twitchIntegration.getToken(), twitchIntegration.getChannelId(), null, false)
                                    .execute().getRewards().forEach(c -> stringBuilder.append(c.getId()).append(" - ").append(c.getTitle()).append("\n"));
                            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                            messageCreateBuilder.setContent(commandEvent.getResource("message.stream-action.points"));
                            messageCreateBuilder.addFiles(FileUpload.fromData(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), "points.txt"));
                            commandEvent.reply(messageCreateBuilder.build());
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.stream-action.noTwitch", BotConfig.getTwitchAuth()));
                        }
                    }

                    default -> commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("stream-action",
                LanguageService.getDefault("command.description.stream-action"))
                .addSubcommands(new SubcommandData("create", "Create a new Stream-Action.")
                        .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true))
                .addSubcommands(new SubcommandData("delete", "Delete a Stream-Action.")
                        .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true))
                .addSubcommands(new SubcommandData("list", "List all Stream-Actions."))
                .addSubcommands(new SubcommandData("points", "List all your ChannelPoint Rewards."))
                .addSubcommandGroups(new SubcommandGroupData("manage", "Manage a existing Stream-action.")
                        .addSubcommands(new SubcommandData("listener", "Set the listener of the Stream-Action.")
                                .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true)
                                .addOption(OptionType.STRING, "listener", "The listener of the Stream-Action.", true),
                                new SubcommandData("delete", "Delete a line of the Stream-Action.")
                                        .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true)
                                        .addOptions(new OptionData(OptionType.INTEGER, "line", "The line of the Stream-Action.", true).setMinValue(1)),
                                new SubcommandData("create", "Create a action in the Stream-Action.")
                                        .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true)
                                        .addOption(OptionType.STRING, "action", "The action of the Stream-Action.", true),
                                new SubcommandData("list", "List all actions of the Stream-Action.")
                                        .addOption(OptionType.STRING, "name", "The name of the Stream-Action.", true)));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
