package de.presti.ree6.commands.impl.community;

import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
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
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

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

                        if (values.length < 2) {
                            return;
                        }

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", values[0]);

                        values = Arrays.stream(values).skip(1).toArray(String[]::new);

                        jsonObject.addProperty("value", String.join(" ", values));

                        streamAction.getActions().getAsJsonArray().add(jsonObject);

                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);

                        commandEvent.reply(commandEvent.getResource("message.stream-action.addedLine", values[0]));
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
                                streamAction.setListener(StreamAction.StreamListener.REDEMPTION);
                            } else if (values[0].equalsIgnoreCase("follow")) {
                                streamAction.setListener(StreamAction.StreamListener.FOLLOW);
                            } else {
                                commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                                return;
                            }

                            if (values.length >= 2)
                                streamAction.setArgument(values[1]);
                            
                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(streamAction);
                        } else {
                            commandEvent.reply(commandEvent.getResource("message.default.missingOption", "manageActionValue"));
                        }
                        break;
                    }

                    case "list": {
                        StreamActionContainer streamActionContainer = new StreamActionContainer(streamAction);
                        commandEvent.reply(commandEvent.getResource("message.stream-action.actionList",
                                streamActionContainer.getActions().entrySet().stream()
                                        .map((Map.Entry<IStreamAction, String[]> entry) ->
                                                entry.getKey().getClass().getAnnotation(StreamActionInfo.class).name() + " -> "
                                                        + String.join(" ", entry.getValue()) + "\n")));
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

            commandEvent.reply(LanguageService.getByEvent(commandEvent, "message.stream-action.list", streamActions.stream().map(c -> c.getActionName() + "\n")));
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
