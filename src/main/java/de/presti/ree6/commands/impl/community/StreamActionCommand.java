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
import de.presti.ree6.streamtools.StreamActionContainer;
import de.presti.ree6.streamtools.action.IStreamAction;
import de.presti.ree6.streamtools.action.StreamActionInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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

        OptionMapping createName = commandEvent.getSlashCommandInteractionEvent().getOption("createname");
        OptionMapping deleteName = commandEvent.getSlashCommandInteractionEvent().getOption("deletename");
        OptionMapping name = commandEvent.getSlashCommandInteractionEvent().getOption("name");
        OptionMapping manageAction = commandEvent.getSlashCommandInteractionEvent().getOption("manageaction");
        OptionMapping manageActionValue = commandEvent.getSlashCommandInteractionEvent().getOption("manageactionvalue");


        if (createName != null) {
            StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new StreamAction(), "SELECT * FROM StreamActions WHERE actionName = :name AND guildId = :gid",
                            Map.of("name", createName.getAsString(), "gid", commandEvent.getGuild().getIdLong()));

            if (streamAction == null) {
                TwitchIntegration twitchIntegration = SQLSession.getSqlConnector().getSqlWorker()
                        .getEntity(new TwitchIntegration(),"SELECT * FROM TwitchIntegration WHERE user_id = :uid", Map.of("uid", commandEvent.getUser().getIdLong()));
                if (twitchIntegration != null) {
                    streamAction = new StreamAction();
                    streamAction.setIntegration(twitchIntegration);
                    streamAction.setGuildId(commandEvent.getGuild().getIdLong());
                    streamAction.setActionName(createName.getAsString());

                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                    commandEvent.reply(commandEvent.getResource("message.stream-action.added", createName.getAsString()));
                } else {
                    commandEvent.reply(commandEvent.getResource("message.stream-action.noTwitch", "https://cp.ree6.de/twitch/auth"));
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.stream-action.alreadyExisting", createName.getAsString()));
            }
        } else if (deleteName != null) {
            StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new StreamAction(), "SELECT * FROM StreamActions WHERE actionName = :name AND guildId = :gid",
                            Map.of("name", deleteName.getAsString(), "gid", commandEvent.getGuild().getIdLong()));
            if (streamAction != null) {
                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(streamAction);
                commandEvent.reply(commandEvent.getResource("message.stream-action.deleted", deleteName.getAsString()));
            } else {
                commandEvent.reply(commandEvent.getResource("message.stream-action.notFound", deleteName.getAsString()));
            }
        } else if (name != null && manageAction != null) {

            StreamAction streamAction = SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new StreamAction(), "SELECT * FROM StreamActions WHERE actionName = :name AND guildId = :gid",
                            Map.of("name", name.getAsString(), "gid", commandEvent.getGuild().getIdLong()));

            if (streamAction != null) {
                switch (manageAction.getAsString()) {
                    case "add": {
                        if (manageActionValue == null) {
                            commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            return;
                        }

                        String[] values = manageActionValue.getAsString().split("\\s+");

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
                        break;
                    }

                    case "listen": {
                        if (manageActionValue == null) {
                            commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            return;
                        }

                        String[] values = manageActionValue.getAsString().split("\\s+");

                        if (values.length >= 1) {
                            if (values[0].equalsIgnoreCase("redemption")) {
                                streamAction.setListener(0);
                            } else if (values[0].equalsIgnoreCase("follow")) {
                                streamAction.setListener(1);
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
                        break;
                    }

                    case "list": {
                        StreamActionContainer streamActionContainer = new StreamActionContainer(streamAction);

                        StringBuilder stringBuilder = new StringBuilder();
                        streamActionContainer.getActions()
                                .forEach(actionRun ->
                                        stringBuilder.append(actionRun.getAction().getClass().getAnnotation(StreamActionInfo.class).name())
                                                .append(" -> ")
                                                .append(String.join(" ", actionRun.getArguments())).append("\n"));

                        commandEvent.reply(commandEvent.getResource("message.stream-action.actionList", stringBuilder.toString()));
                        break;
                    }

                    case "points": {
                        TwitchIntegration twitchIntegration = SQLSession.getSqlConnector().getSqlWorker()
                                .getEntity(new TwitchIntegration(), "SELECT * FROM TwitchIntegration WHERE user_id = :uid", Map.of("uid", commandEvent.getUser().getIdLong()));
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
                            commandEvent.reply(commandEvent.getResource("message.stream-action.noTwitch", "https://cp.ree6.de/twitch/auth"));
                        }
                        break;
                    }

                    case "delete": {
                        if (manageActionValue == null) {
                            commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            return;
                        }

                        try {
                            int value = manageActionValue.getAsInt();
                            if (streamAction.getActions().getAsJsonArray().size() >= value && value > 0) {
                                streamAction.getActions().getAsJsonArray().remove(value -1);
                                SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                                commandEvent.reply(commandEvent.getResource("message.stream-action.deletedLine", "" + value));
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                            }
                        } catch (Exception exception) {
                            commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                        }
                        break;
                    }

                    default: {
                        commandEvent.reply(commandEvent.getResource("message.default.invalidOption"));
                        break;
                    }
                }
            } else {
                commandEvent.reply(commandEvent.getResource("message.stream-action.notFound", name.getAsString()));
            }
        } else {
            List<StreamAction> streamActions = SQLSession.getSqlConnector().getSqlWorker()
                    .getEntityList(new StreamAction(), "SELECT * FROM StreamActions WHERE guildId = :gid",
                            Map.of("gid", commandEvent.getGuild().getIdLong()));

            commandEvent.reply(LanguageService.getByEvent(commandEvent, "message.stream-action.list", String.join("\n", streamActions.stream().map(StreamAction::getActionName).toArray(String[]::new))));
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("stream-action",
                LanguageService.getDefault("command.description.stream-action"))
                .addOption(OptionType.STRING, "createname", "The name of the to be created action")
                .addOption(OptionType.STRING, "deletename", "The name of the to be deleted action")
                .addOption(OptionType.STRING, "name", "The name of the already created action")
                .addOption(OptionType.STRING, "manageaction", "The managing action that should be performed on the Stream-Action")
                .addOption(OptionType.STRING, "manageactionvalue", "The value of the managing action");
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
