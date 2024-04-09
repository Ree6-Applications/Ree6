package de.presti.ree6.commands;

import de.presti.ree6.commands.exceptions.CommandInitializerException;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.custom.CustomCommand;
import de.presti.ree6.sql.util.SettingsManager;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.ThreadUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Manager class used to manage all Commands and command related operation.
 */
@Slf4j
public class CommandManager {

    /**
     * An Arraylist with all registered Commands.
     */
    @Getter(AccessLevel.PUBLIC)
    private final ArrayList<ICommand> commands = new ArrayList<>();

    /**
     * Constructor for the Command-Manager used to register every Command.
     *
     * @throws CommandInitializerException if an error occurs while initializing the Commands.
     * @throws IllegalStateException       if an Invalid Command was used to initialize.
     * @throws IllegalAccessException      when an Instance of a Command is not accessible.
     * @throws InstantiationException      when an Instance of a Command is not instantiable.
     * @throws NoSuchMethodException       when a Constructor Instance of a Command is not found.
     * @throws InvocationTargetException   when a Constructor Instance of a Command is not found.
     */
    public CommandManager() throws CommandInitializerException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        log.info("Initializing Commands!");

        Reflections reflections = new Reflections("de.presti.ree6.commands");
        Set<Class<? extends ICommand>> classes = reflections.getSubTypesOf(ICommand.class);

        for (Class<? extends ICommand> aClass : classes) {
            Command commandAnnotation = aClass.getAnnotation(Command.class);

            if (!BotConfig.isModuleActive(commandAnnotation.category().getDescription().substring(9).toLowerCase()))
                continue;

            log.info("Loading Command {}", aClass.getSimpleName());

            addCommand(aClass.getDeclaredConstructor().newInstance());
        }

        if (!BotConfig.isModuleActive("ai")) return;

        StringBuilder stringBuilder = new StringBuilder();


        stringBuilder.append("Commands").append("\n");

        for (ICommand command : commands) {
            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            if (commandAnnotation.category() == Category.HIDDEN) continue;

            stringBuilder.append("Command ").append("Name ").append(commandAnnotation.name()).append(" Desc ")
                    .append(commandAnnotation.description()).append("\n");

            CommandDataImpl commandData = (CommandDataImpl) command.getCommandData();

            if (commandData == null) {
                if (commandAnnotation.category() == Category.HIDDEN) continue;

                commandData = new CommandDataImpl(command.getClass().getAnnotation(Command.class).name(), command.getClass().getAnnotation(Command.class).description());
            }


            if (!commandData.getSubcommands().isEmpty()) {
                stringBuilder.append(convertSlashSubToString(commandData.getSubcommands())).append("\n");
            }

            if (!commandData.getSubcommandGroups().isEmpty()) {
                stringBuilder.append("Groups ").append("\n");

                for (SubcommandGroupData subcommandGroupData : commandData.getSubcommandGroups()) {
                    stringBuilder.append("Subcommand Group ")
                            .append("Name ").append(subcommandGroupData.getName())
                            .append(" Desc ").append(subcommandGroupData.getDescription())
                            .append("\n");

                    stringBuilder.append(convertSlashSubToString(subcommandGroupData.getSubcommands()));
                }

            } else {
                if (!commandData.getSubcommands().isEmpty()) {
                    stringBuilder.append(convertSlashSubToString(commandData.getSubcommands()));
                } else {
                    stringBuilder.append(convertOptionsToString(commandData.getOptions()));
                }
            }
        }

        Main.getInstance().getChatGPTAPI().updatePreDefinedText(stringBuilder.toString());
    }

    /**
     * Used to convert Slash Subcommands to a String format for ChatGPT.
     *
     * @param list a List with all the Subcommands.
     * @return the List as a string.
     */
    private String convertSlashSubToString(List<SubcommandData> list) {
        if (list.isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Subcommands ").append("\n");

        for (SubcommandData subcommandData : list) {
            stringBuilder.append("Subcommand ").append("Name ").append(subcommandData.getName())
                    .append(" Desc ").append(subcommandData.getDescription())
                    .append("\n");

            stringBuilder.append(convertOptionsToString(subcommandData.getOptions()));
        }

        return stringBuilder.toString();
    }

    /**
     * Used to convert Slash Options to a String format for ChatGPT.
     *
     * @param list a List with all the Options.
     * @return the List as a string.
     */
    private String convertOptionsToString(List<OptionData> list) {
        if (list.isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Options ").append("\n");
        for (OptionData optionData : list) {
            stringBuilder.append("Option ").append("Name ").append(optionData.getName())
                    .append(" Des ").append(optionData.getDescription())
                    .append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Method used to add all Commands as SlashCommand on Discord.
     *
     * @param jda Instance of the Bot.
     */
    public void addSlashCommand(JDA jda) {
        if (!BotConfig.isModuleActive("slashcommands")) return;

        CommandListUpdateAction listUpdateAction = jda.updateCommands();

        for (ICommand command : getCommands()) {
            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            CommandData commandData;

            if (command.getCommandData() != null) {
                commandData = command.getCommandData();
            } else {
                if (commandAnnotation.category() == Category.HIDDEN) continue;

                commandData = new CommandDataImpl(command.getClass().getAnnotation(Command.class).name(), command.getClass().getAnnotation(Command.class).description());
            }

            if (commandAnnotation.category() == Category.NSFW) {
                commandData.setNSFW(true);
            }

            if (commandData instanceof CommandDataImpl commandData1) {

                for (DiscordLocale discordLocale : DiscordLocale.values()) {
                    if (!LanguageService.languageResources.containsKey(discordLocale)) continue;

                    if (commandAnnotation.description().endsWith(".") || !commandAnnotation.description().contains("."))
                        continue;

                    String description = LanguageService.getByLocale(discordLocale, commandAnnotation.description());

                    if (description.equals("Missing language resource!")) {
                        description = LanguageService.getDefault(commandAnnotation.description());
                    }

                    if (!description.equals("Missing language resource!")) {
                        commandData1.setDescriptionLocalization(discordLocale, description);
                    }

                    commandData1.getSubcommandGroups().forEach(subcommandGroupData -> translateSubgroups(subcommandGroupData, discordLocale));
                }

                String description = LanguageService.getDefault(commandAnnotation.description());

                if (!description.equals("Missing language resource!")) {
                    commandData1.setDescription(description);
                }

                // TODO:: add the same language check to option names/description and add a translation to it. Also for the love of god Imma need to optimize this.

                if (commandAnnotation.category() == Category.MOD && commandData.getDefaultPermissions() == DefaultMemberPermissions.ENABLED) {
                    commandData1.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
                }

                commandData1.setGuildOnly(true);

                //noinspection ResultOfMethodCallIgnored
                listUpdateAction.addCommands(commandData1);
            } else {
                if (commandAnnotation.category() == Category.MOD && commandData.getDefaultPermissions() == DefaultMemberPermissions.ENABLED) {
                    commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
                }

                commandData.setGuildOnly(true);

                //noinspection ResultOfMethodCallIgnored
                listUpdateAction.addCommands(commandData);
            }
        }

        listUpdateAction.queue();
    }

    public void translateSubgroups(SubcommandGroupData subcommandGroupData, DiscordLocale locale) {
        String groupDescription = subcommandGroupData.getDescription();

        if (groupDescription.contains(".") && !groupDescription.endsWith(".")) {
            groupDescription = LanguageService.getByLocale(locale, groupDescription);

            if (groupDescription.equals("Missing language resource!")) {
                groupDescription = LanguageService.getDefault(subcommandGroupData.getDescription());
            }

            if (!groupDescription.equals("Missing language resource!")) {
                subcommandGroupData.setDescriptionLocalization(locale, groupDescription);
            }
        }

        for (SubcommandData subcommandData : subcommandGroupData.getSubcommands()) {
            String commandDescription = subcommandData.getDescription();

            if (commandDescription.contains(".") && !commandDescription.endsWith(".")) {
                commandDescription = LanguageService.getByLocale(locale, commandDescription);

                if (commandDescription.equals("Missing language resource!")) {
                    commandDescription = LanguageService.getDefault(subcommandData.getDescription());
                }

                if (!commandDescription.equals("Missing language resource!")) {
                    subcommandData.setDescriptionLocalization(locale, commandDescription);
                }
            }
        }
    }

    /**
     * Add a single Command to the Command list.
     *
     * @param command the {@link ICommand}.
     * @throws CommandInitializerException if an error occurs while initializing the Command.
     */
    public void addCommand(ICommand command) throws CommandInitializerException {
        if (!command.getClass().isAnnotationPresent(Command.class) || command.getClass().getAnnotation(Command.class).category() == null)
            throw new CommandInitializerException(command.getClass());

        if (!commands.contains(command)) {
            commands.add(command);

            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            // Skip the hidden Commands.
            if (commandAnnotation.category() == Category.HIDDEN) return;

            SettingsManager.getSettings().add(new Setting(-1,
                    "command_" + commandAnnotation.name().toLowerCase(), commandAnnotation.name(), true));
        }
    }

    /**
     * Get a Command by its name.
     *
     * @param name the Name of the Command.
     * @return the {@link ICommand} with the same Name.
     */
    public ICommand getCommandByName(String name) {
        return getCommands().stream().filter(command -> command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name) || Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(name))).findFirst().orElse(null);

    }

    /**
     * Get a Command by its slash command name.
     *
     * @param name the Name of the Command.
     * @return the {@link ICommand} with the same Name.
     */
    public ICommand getCommandBySlashName(String name) {
        return getCommands().stream().filter(command -> (command.getCommandData() != null && command.getCommandData().getName().equalsIgnoreCase(name)) || (command.getClass().isAnnotationPresent(Command.class) && command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    /**
     * Remove a Command from the List.
     *
     * @param command the Command you want to remove.
     */
    @SuppressWarnings("unused")
    public void removeCommand(ICommand command) {
        commands.remove(command);
    }

    /**
     * Try to perform a Command.
     *
     * @param member                       the Member that performed the try.
     * @param guild                        the Guild the Member is from.
     * @param messageContent               the Message content (including the prefix + command name).
     * @param message                      the Message Entity.
     * @param messageChannel               the MessageChannel where the command has been performed.
     * @param slashCommandInteractionEvent the Slash Command Event if it was a Slash Command.
     * @return true, if a command has been performed.
     */
    public boolean perform(Member member, Guild guild, String messageContent, Message message, GuildMessageChannelUnion messageChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        // Check if the User is under Cooldown.
        if (isTimeout(member.getUser())) {

            // Check if it is a Slash Command or not.
            if (slashCommandInteractionEvent != null) {
                sendMessage(LanguageService.getByGuild(guild, "command.perform.cooldown"), 5, messageChannel, slashCommandInteractionEvent.getHook().setEphemeral(true));
                deleteMessage(message, slashCommandInteractionEvent.getHook().setEphemeral(true));
            } else if (messageContent.toLowerCase().startsWith(SQLSession.getSqlConnector().getSqlWorker().getSetting(guild.getIdLong(), "chatprefix").getStringValue().toLowerCase())) {
                sendMessage(LanguageService.getByGuild(guild, "command.perform.cooldown"), 5, messageChannel, null);
                deleteMessage(message, null);
            }

            // Return false.
            return false;
        }

        // Check if it is a Slash Command.
        if (slashCommandInteractionEvent != null) {
            if (!BotConfig.isModuleActive("slashcommands")) return false;
            if (!performSlashCommand(messageChannel, slashCommandInteractionEvent)) {
                return false;
            }
        } else {
            if (!BotConfig.isModuleActive("messagecommands")) return false;
            if (!performMessageCommand(member, guild, messageContent, message, messageChannel)) {
                return false;
            }
        }

        // Check if this is a Developer build, if not then cooldown the User.
        if (!BotConfig.isDebug()) {
            ThreadUtil.createThread(x -> ArrayUtil.commandCooldown.remove(member.getUser().getId()), null, Duration.ofSeconds(5), false, false);
        }

        // Add them to the Cooldown.
        if (!ArrayUtil.commandCooldown.contains(member.getUser().getId()) && !BotConfig.isDebug()) {
            ArrayUtil.commandCooldown.add(member.getUser().getId());
        }

        // Return that a command has been performed.
        return true;
    }

    /**
     * Perform a Message based Command.
     *
     * @param member         the Member that performed the command.
     * @param guild          the Guild the Member is from.
     * @param messageContent the Message content (including the prefix + command name).
     * @param message        the Message Entity.
     * @param textChannel    the TextChannel where the command has been performed.
     * @return true, if a command has been performed.
     */
    private boolean performMessageCommand(Member member, Guild guild, String messageContent, Message message, GuildMessageChannelUnion textChannel) {
        // Check if the Message is null.
        if (message == null) {
            sendMessage(LanguageService.getByGuild(guild, "command.perform.error"), 5, textChannel, null);
            return false;
        }

        String currentPrefix = SQLSession.getSqlConnector().getSqlWorker().getSetting(guild.getIdLong(), "chatprefix").getStringValue().toLowerCase();

        // Check if the message starts with the prefix.
        if (!messageContent.toLowerCase().startsWith(currentPrefix))
            return false;

        // Parse the Message and remove the prefix from it.
        messageContent = messageContent.substring(currentPrefix.length());

        // Split all Arguments.
        String[] arguments = messageContent.split("\\s+");

        if (arguments.length == 0 || arguments[0].isBlank()) {
            sendMessage(LanguageService.getByGuild(guild, "command.perform.missingCommand"), 5, textChannel, null);
            return false;
        }

        // Get the Command by the name.
        ICommand command = getCommandByName(arguments[0]);

        // Check if there is even a Command with that name.
        if (command == null && BotConfig.isModuleActive("customcommands")) {
            CustomCommand customCommand = SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(), "FROM CustomCommand WHERE guildId=:gid AND name=:command", Map.of("gid", guild.getIdLong(), "command", arguments[0].toLowerCase()));
            if (customCommand != null) {
                GuildMessageChannelUnion messageChannelUnion = textChannel;

                if (customCommand.getChannelId() != -1) {
                    messageChannelUnion = guild.getChannelById(GuildMessageChannelUnion.class, customCommand.getChannelId());
                }

                if (customCommand.getMessageResponse() != null) {
                    sendMessage(customCommand.getMessageResponse(), 5, messageChannelUnion, null);
                }

                if (customCommand.getEmbedResponse() != null) {
                    EmbedBuilder embedBuilder = EmbedBuilder.fromData(DataObject.fromJson(customCommand.getEmbedResponse().toString()));
                    sendMessage(embedBuilder, 5, messageChannelUnion, null);
                }

                return true;
            }

            sendMessage(LanguageService.getByGuild(guild, "command.perform.notFound"), 5, textChannel, null);
            return false;
        } else if (command == null) {
            sendMessage(LanguageService.getByGuild(guild, "command.perform.notFound"), 5, textChannel, null);
            return false;
        }

        if (command.getClass().getAnnotation(Command.class).category() != Category.HIDDEN) {
            Setting blacklistSetting = SQLSession.getSqlConnector().getSqlWorker().getSetting(guild.getIdLong(), "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase());

            // Check if the Command is blacklisted.
            if (blacklistSetting != null && !blacklistSetting.getBooleanValue()) {
                sendMessage(LanguageService.getByGuild(guild, "command.perform.blocked"), 5, textChannel, null);
                return false;
            }
        }

        // Parse the arguments.
        String[] argumentsParsed = Arrays.copyOfRange(arguments, 1, arguments.length);

        // Perform the Command.
        command.onASyncPerform(new CommandEvent(command.getClass().getAnnotation(Command.class).name(), member, guild, message, textChannel, argumentsParsed, null));

        return true;
    }

    /**
     * Call when a slash command has been performed.
     *
     * @param messageChannel               the {@link GuildMessageChannelUnion} where the command has been performed.
     * @param slashCommandInteractionEvent the Slash-Command Event.
     * @return true, if a command has been performed.
     */
    private boolean performSlashCommand(GuildMessageChannelUnion messageChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        //Get the Command by the Slash Command Name.
        ICommand command = getCommandBySlashName(slashCommandInteractionEvent.getName());

        // Check if there is a command with that Name.
        if (command == null || slashCommandInteractionEvent.getGuild() == null || slashCommandInteractionEvent.getMember() == null) {
            sendMessage(LanguageService.getByGuild(slashCommandInteractionEvent.getGuild(), "command.perform.notFound"), 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
            return false;
        }

        if (command.getClass().getAnnotation(Command.class).category() != Category.HIDDEN) {
            Setting blacklistSetting = SQLSession.getSqlConnector().getSqlWorker().getSetting(slashCommandInteractionEvent.getGuild().getIdLong(), "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase());

            // Check if the Command is blacklisted.
            if (blacklistSetting != null && !blacklistSetting.getBooleanValue()) {
                sendMessage(LanguageService.getByGuild(slashCommandInteractionEvent.getGuild(), "command.perform.blocked"), 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
                return false;
            }
        }

        // Perform the Command.
        command.onASyncPerform(new CommandEvent(command.getClass().getAnnotation(Command.class).name(), slashCommandInteractionEvent.getMember(), slashCommandInteractionEvent.getGuild(), null, messageChannel, null, slashCommandInteractionEvent));

        return true;
    }

    /**
     * Check if a User is time-outed.
     *
     * @param user the User.
     * @return true, if yes | false, if not.
     */
    public boolean isTimeout(User user) {
        return ArrayUtil.commandCooldown.contains(user.getId()) && !BotConfig.isDebug();
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param commandEvent      the Command-Event.
     */
    public void sendMessage(MessageCreateData messageCreateData, CommandEvent commandEvent) {
        sendMessage(messageCreateData, commandEvent.getChannel(), commandEvent.getInteractionHook());
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param deleteSecond      the delete delay
     * @param commandEvent      the Command-Event.
     */
    public void sendMessage(MessageCreateData messageCreateData, int deleteSecond, CommandEvent commandEvent) {
        sendMessage(messageCreateData, deleteSecond, commandEvent.getChannel(), commandEvent.getInteractionHook());
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     */
    public void sendMessage(MessageCreateData messageCreateData, MessageChannel messageChannel) {
        sendMessage(messageCreateData, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param messageCreateData the Message content.
     * @param deleteSecond      the delete delay
     * @param messageChannel    the Message-Channel.
     */
    public void sendMessage(MessageCreateData messageCreateData, int deleteSecond, MessageChannel messageChannel) {
        sendMessage(messageCreateData, deleteSecond, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     * @param interactionHook   the Interaction-hook if it is a slash command.
     */
    public void sendMessage(MessageCreateData messageCreateData, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel.canTalk()) messageChannel.sendMessage(messageCreateData).queue();
        } else interactionHook.sendMessage(messageCreateData).queue();
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param messageCreateData the Message content.
     * @param messageChannel    the Message-Channel.
     * @param interactionHook   the Interaction-hook if it is a slash command.
     * @param deleteSecond      the delete delay
     */
    public void sendMessage(MessageCreateData messageCreateData, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel == null) return;
            if (messageChannel.canTalk())
                messageChannel.sendMessage(messageCreateData).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                    if (message != null && message.getChannel().retrieveMessageById(message.getId()).complete() != null) {
                        return message.delete();
                    }

                    return null;
                }).queue();
        } else {
            interactionHook.sendMessage(messageCreateData).queue();
        }
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message        the Message content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(String message, MessageChannel messageChannel) {
        sendMessage(message, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param message        the Message content.
     * @param deleteSecond   the delete delay
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(String message, int deleteSecond, MessageChannel messageChannel) {
        sendMessage(message, deleteSecond, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message         the Message content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(String message, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setContent(message).build(), messageChannel, interactionHook);
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param messageContent  the Message content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     * @param deleteSecond    the delete delay
     */
    public void sendMessage(String messageContent, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setContent(messageContent).build(), deleteSecond, messageChannel, interactionHook);
    }

    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder   the Embed content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel) {
        sendMessage(embedBuilder, messageChannel, null);
    }

    /**
     * Send an Embed to a special Message-Channel, with a deletion delay.
     *
     * @param embedBuilder   the Embed content.
     * @param deleteSecond   the delete delay
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(EmbedBuilder embedBuilder, int deleteSecond, MessageChannel messageChannel) {
        sendMessage(embedBuilder, deleteSecond, messageChannel, null);
    }

    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder    the Embed content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setEmbeds(embedBuilder.build()).build(), messageChannel, interactionHook);
    }

    /**
     * Send an Embed to a special Message-Channel, with a deletion delay.
     *
     * @param embedBuilder    the Embed content.
     * @param deleteSecond    the delete delay
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(EmbedBuilder embedBuilder, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        sendMessage(new MessageCreateBuilder().setEmbeds(embedBuilder.build()).build(), deleteSecond, messageChannel, interactionHook);
    }

    /**
     * Delete a specific message.
     *
     * @param message         the {@link Message} entity.
     * @param interactionHook the Interaction-hook, if it is a slash event.
     */
    public void deleteMessage(Message message, InteractionHook interactionHook) {
        if (message != null && message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                message.getChannel().retrieveMessageById(message.getIdLong()).complete() != null &&
                message.getType().canDelete() &&
                !message.isEphemeral() &&
                interactionHook == null) {
            message.delete().onErrorMap(throwable -> {
                log.error("[CommandManager] Couldn't delete a Message!", throwable);
                return null;
            }).queue();
        }
    }

}