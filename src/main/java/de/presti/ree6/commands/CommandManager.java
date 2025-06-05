package de.presti.ree6.commands;

import de.presti.ree6.bot.BotConfig;
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
import de.presti.ree6.utils.data.RegExUtil;
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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.reflections.Reflections;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
     * @param shardManager the Shard manager.
     */
    public void addSlashCommand(ShardManager shardManager)  {
        for (JDA jda : shardManager.getShards()){
            addSlashCommand(jda);
        }
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

            if (commandAnnotation.category().isGuildOnly()) {
                commandData.setIntegrationTypes(IntegrationType.GUILD_INSTALL);
            } else {
                if (commandAnnotation.allowAppInstall()) {
                    commandData.setIntegrationTypes(IntegrationType.ALL);
                } else {
                    commandData.setIntegrationTypes(IntegrationType.GUILD_INSTALL);
                }
            }

            if (commandData instanceof CommandDataImpl commandData1) {

                boolean isValidDescription = commandAnnotation.description().matches(RegExUtil.ALLOWED_LANGUAGE_PATHS);

                for (DiscordLocale discordLocale : DiscordLocale.values()) {
                    if (!LanguageService.languageResources.containsKey(discordLocale)) continue;

                    if (!isValidDescription)
                        continue;

                    String localizedDescription = LanguageService.getByLocale(discordLocale, commandAnnotation.description()).block();

                    if (localizedDescription != null && localizedDescription.equals("Missing language resource!")) {
                        localizedDescription = LanguageService.getDefault(commandAnnotation.description()).block();
                    }

                    if (localizedDescription != null && !localizedDescription.equals("Missing language resource!")) {
                        commandData1.setDescriptionLocalization(discordLocale, localizedDescription);
                    }

                    commandData1.getSubcommandGroups().forEach(subcommandGroupData -> translateSubgroups(subcommandGroupData, discordLocale));
                }

                if (isValidDescription) {
                    String localizedDescription = LanguageService.getDefault(commandAnnotation.description()).block();

                    if (localizedDescription != null && !localizedDescription.equals("Missing language resource!")) {
                        commandData1.setDescription(localizedDescription);
                    }
                }

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

                commandData.setContexts(InteractionContextType.GUILD);

                //noinspection ResultOfMethodCallIgnored
                listUpdateAction.addCommands(commandData);
            }
        }

        listUpdateAction.queue();
    }

    private void translateSubgroups(SubcommandGroupData subcommandGroupData, DiscordLocale locale) {
        String groupDescription = subcommandGroupData.getDescription();

        if (groupDescription.matches(RegExUtil.ALLOWED_LANGUAGE_PATHS)) {
            if (LanguageService.hasTranslation(locale, groupDescription).block()) {
                groupDescription = LanguageService.getByLocale(locale, groupDescription).block();
            } else if (LanguageService.hasDefaultTranslation(groupDescription).block()) {
                groupDescription = LanguageService.getDefault(groupDescription).block();
            }

            subcommandGroupData.setDescriptionLocalization(locale, groupDescription);
        }

        // Translate Subcommands
        for (SubcommandData subcommandData : subcommandGroupData.getSubcommands()) {
            translateSubcomand(subcommandData, locale);
        }
    }

    private void translateSubcomand(SubcommandData subcommandData, DiscordLocale locale) {
        String commandDescription = subcommandData.getDescription();

        if (commandDescription != null && commandDescription.matches(RegExUtil.ALLOWED_LANGUAGE_PATHS)) {
            if (LanguageService.hasTranslation(locale, commandDescription).block()) {
                commandDescription = LanguageService.getByLocale(locale, commandDescription).block();
            } else if (LanguageService.hasDefaultTranslation(commandDescription).block()) {
                commandDescription = LanguageService.getDefault(commandDescription).block();
            }

            subcommandData.setDescriptionLocalization(locale, commandDescription);
        }

        // Translate Options
        for (OptionData optionData : subcommandData.getOptions()) {
            translateOption(optionData, locale);
        }
    }

    private void translateOption(OptionData optionData, DiscordLocale locale) {
        // Name translation
        String optionName = optionData.getName();
        if (optionName != null && optionName.matches(RegExUtil.ALLOWED_LANGUAGE_PATHS)) {
            if (LanguageService.hasTranslation(locale, optionName).block()) {
                optionName = LanguageService.getByLocale(locale, optionName).block();
            } else if (LanguageService.hasDefaultTranslation(optionName).block()) {
                optionName = LanguageService.getDefault(optionName).block();
            }

            optionData.setNameLocalization(locale, optionName);
        }

        // Description translation
        String optionDescription = optionData.getDescription();
        if (optionDescription != null && optionDescription.matches(RegExUtil.ALLOWED_LANGUAGE_PATHS)) {
            if (LanguageService.hasTranslation(locale, optionDescription).block()) {
                optionDescription = LanguageService.getByLocale(locale, optionDescription).block();
            } else if (LanguageService.hasDefaultTranslation(optionDescription).block()) {
                optionDescription = LanguageService.getDefault(optionDescription).block();
            }

            optionData.setDescriptionLocalization(locale, optionDescription);
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
            log.info("Loaded Command {}", command.getClass().getSimpleName());

            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            // Skip the hidden Commands.
            if (commandAnnotation.category() == Category.HIDDEN) return;

            SettingsManager.getSettings().add(new Setting(-1,
                    "command_" + commandAnnotation.name().toLowerCase(), commandAnnotation.name(), true));
        } else {
            throw new CommandInitializerException(command.getClass());
        }
    }

    /**
     * Get a Command by its name.
     *
     * @param name the Name of the Command.
     * @return the {@link ICommand} with the same Name.
     */
    public ICommand getCommandByName(String name) {
        return getCommands().stream().filter(command -> command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name) ||
                Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(name))).findFirst().orElse(null);

    }

    /**
     * Get a Command by its slash command name.
     *
     * @param name the Name of the Command.
     * @return the {@link ICommand} with the same Name.
     */
    public ICommand getCommandBySlashName(String name) {
        return getCommands().stream().filter(command -> (command.getCommandData() != null && command.getCommandData().getName().equalsIgnoreCase(name)) ||
                (command.getClass().isAnnotationPresent(Command.class) && command.getClass().getAnnotation(Command.class).name().equalsIgnoreCase(name))).findFirst().orElse(null);
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
    public Mono<Boolean> perform(Member member, Guild guild, String messageContent, Message message, GuildMessageChannelUnion messageChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        boolean isSlashCommand = slashCommandInteractionEvent != null;

        if (!isSlashCommand && guild.isDetached()) {
            return Mono.just(false);
        }

        if (BotConfig.isDebug())
            log.info("Called perform");

        // Check if the User is under Cooldown.
        if (isTimeout(member.getUser())) {

            // Check if it is a Slash Command or not.
            if (isSlashCommand) {
                return LanguageService.getByGuild(guild, "command.perform.cooldown").map(messageCreateData -> {
                    slashCommandInteractionEvent.getHook().sendMessage(messageCreateData).queue();
                    deleteMessage(message, slashCommandInteractionEvent.getHook().setEphemeral(true));
                    return false;
                });
            } else {
                return SQLSession.getSqlConnector().getSqlWorker().getSetting(guild.getIdLong(), "chatprefix").publishOn(Schedulers.boundedElastic()).mapNotNull(setting -> {
                    if (setting.isPresent() && messageContent.toLowerCase().startsWith(setting.get().getStringValue().toLowerCase())) {
                        final Mono<Boolean> booleanMono = LanguageService.getByGuild(guild, "command.perform.cooldown").map(translation -> {
                            sendMessage(String.valueOf(translation), 5, messageChannel, null);
                            deleteMessage(message, null);
                            return false;
                        }).thenReturn(false);
                        return booleanMono.block();
                    }

                    return false;
                });
            }
        }

        // Check if it is a Slash Command.
        if (isSlashCommand) {
            if (!BotConfig.isModuleActive("slashcommands")) return Mono.just(false);
            return performSlashCommand(messageChannel, slashCommandInteractionEvent);
        } else {
            if (!BotConfig.isModuleActive("messagecommands")) return Mono.just(false);
            return performMessageCommand(member, guild, messageContent, message, messageChannel);
        }
    }

    public void timeoutUser(User user) {
        // Check if this is a Developer build, if not then cooldown the User.
        if (!BotConfig.isDebug()) return;

        ThreadUtil.createThread(x -> ArrayUtil.commandCooldown.remove(user.getId()), null, Duration.ofSeconds(5), false, false);

        // Add them to the Cooldown.
        if (!ArrayUtil.commandCooldown.contains(user.getId())) {
            ArrayUtil.commandCooldown.add(user.getId());
        }
    }

    /**
     * Perform a Message-based Command.
     *
     * @param member         the Member that performed the command.
     * @param guild          the Guild the Member is from.
     * @param messageContent the Message content (including the prefix + command name).
     * @param message        the Message Entity.
     * @param textChannel    the TextChannel where the command has been performed.
     * @return true, if a command has been performed.
     */
    private Mono<Boolean> performMessageCommand(Member member, Guild guild, String messageContent, Message message, GuildMessageChannelUnion textChannel) {
        // Check if the Message is null.
        if (message == null) {
            if (BotConfig.isDebug())
                log.info("Message is null.");
            sendMessage(LanguageService.getByGuild(guild, "command.perform.error").block(), 5, textChannel, null);
            return Mono.just(false);
        }

        if (BotConfig.isDebug())
            log.info("Called performMessageCommand");

        return SQLSession.getSqlConnector().getSqlWorker().getSetting(guild.getIdLong(), "chatprefix").publishOn(Schedulers.boundedElastic()).mapNotNull(setting -> {

            if (BotConfig.isDebug())
                log.info("Got to prefix check.");

            String currentPrefix = setting.orElseGet(() -> new Setting(-1, "chatprefix", "Chat Prefix", BotConfig.getDefaultPrefix())).getStringValue();

            // Check if the message starts with the prefix.
            if (!messageContent.toLowerCase().startsWith(currentPrefix)) {
                if (BotConfig.isDebug())
                    log.info("Wrong prefix");
                return false;
            }

            // Split all Arguments.
            String[] arguments = messageContent.substring(currentPrefix.length()).split("\\s+");

            if (arguments.length == 0 || arguments[0].isBlank()) {
                return LanguageService.getByGuild(guild, "command.perform.missingCommand").map(translated -> {
                    sendMessage(translated, 5, textChannel, null);
                    if (BotConfig.isDebug())
                        log.info("Missing command from string.");
                    return false;
                }).block();
            }

            if (BotConfig.isDebug())
                log.info("Passed parsing.");

            // Get the Command by the name.
            ICommand command = getCommandByName(arguments[0]);

            // Check if there is even a Command with that name.
            if (command == null && BotConfig.isModuleActive("customcommands")) {
                return SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(), "FROM CustomCommand WHERE guildId=:gid AND name=:command",
                                Map.of("gid", guild.getIdLong(), "command", arguments[0].toLowerCase()))
                        .flatMap(customCommand -> {
                            if (BotConfig.isDebug())
                                log.info("Got custom command.");

                            if (customCommand.isPresent()) {
                                if (BotConfig.isDebug())
                                    log.info("Custom command is present.");
                                GuildMessageChannelUnion messageChannelUnion = textChannel;
                                CustomCommand customCommandEntity = customCommand.get();
                                if (customCommandEntity.getChannelId() != -1) {
                                    messageChannelUnion = guild.getChannelById(GuildMessageChannelUnion.class, customCommandEntity.getChannelId());
                                }

                                if (customCommandEntity.getMessageResponse() != null) {
                                    sendMessage(customCommandEntity.getMessageResponse(), 5, messageChannelUnion, null);
                                }

                                if (customCommandEntity.getEmbedResponse() != null) {
                                    EmbedBuilder embedBuilder = EmbedBuilder.fromData(DataObject.fromJson(customCommandEntity.getEmbedResponse().toString()));
                                    sendMessage(embedBuilder, 5, messageChannelUnion, null);
                                }

                                return Mono.just(true);
                            }

                            if (BotConfig.isDebug())
                                log.info("Custom command is not present.");

                            return LanguageService.getByGuild(guild, "command.perform.notFound").map(translated -> {
                                sendMessage(translated, 5, textChannel, null);
                                return false;
                            });
                        }).block();
            } else if (command == null) {
                return LanguageService.getByGuild(guild, "command.perform.notFound").map(translated -> {
                    sendMessage(translated, 5, textChannel, null);
                    return false;
                }).block();
            }

            if (BotConfig.isDebug())
                log.info("Finished command check.");

            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            if (commandAnnotation.category() != Category.HIDDEN) {
                Optional<Setting> blacklist = SQLSession.getSqlConnector().getSqlWorker()
                        .getSetting(guild.getIdLong(), "command_" + commandAnnotation.name().toLowerCase()).block();

                // Check if the Command is blacklisted.
                if (blacklist != null && blacklist.isPresent() && !blacklist.get().getBooleanValue()) {
                    return LanguageService.getByGuild(guild, "command.perform.blocked").map(translated -> {
                        sendMessage(translated, 5, textChannel, null);
                        return false;
                    }).block();
                }
            }

            if (BotConfig.isDebug())
                log.info("Finished blacklist.");

            // Parse the arguments.
            String[] argumentsParsed = Arrays.copyOfRange(arguments, 1, arguments.length);

            // Perform the Command.
            return command.onMonoPerform(new CommandEvent(commandAnnotation.name(), member, guild, message, textChannel, argumentsParsed, null, true)).block();
        });
    }

    /**
     * Call when a slash command has been performed.
     *
     * @param messageChannel               the {@link GuildMessageChannelUnion} where the command has been performed.
     * @param slashCommandInteractionEvent the Slash-Command Event.
     * @return true, if a command has been performed.
     */
    private Mono<Boolean> performSlashCommand(GuildMessageChannelUnion messageChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {
        //Get the Command by the Slash Command Name.
        ICommand command = getCommandBySlashName(slashCommandInteractionEvent.getName());

        // Check if there is a command with that Name.
        if (command == null || slashCommandInteractionEvent.getGuild() == null || slashCommandInteractionEvent.getMember() == null) {
            sendMessage(LanguageService.getByGuild(slashCommandInteractionEvent.getGuild(), "command.perform.notFound").block(), 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
            return Mono.just(false);
        }

        Command annotation = command.getClass().getAnnotation(Command.class);

        Guild guild = slashCommandInteractionEvent.getGuild();

        if (!annotation.allowAppInstall() && guild.isDetached()) return Mono.just(false);

        CommandEvent commandEvent = new CommandEvent(annotation.name(), slashCommandInteractionEvent.getMember(), guild, null, messageChannel, null, slashCommandInteractionEvent, true);

        if (guild.isDetached()) {
            return command.onMonoPerform(commandEvent);
        } else {
            return SQLSession.getSqlConnector().getSqlWorker().getSetting(slashCommandInteractionEvent.getGuild().getIdLong(), "command_" + annotation.name().toLowerCase()).publishOn(Schedulers.boundedElastic()).mapNotNull(setting -> {
                if (annotation.category() != Category.HIDDEN && setting.isPresent() && !setting.get().getBooleanValue()) {
                    sendMessage(LanguageService.getByGuild(slashCommandInteractionEvent.getGuild(), "command.perform.blocked").block(), 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
                    return false;
                }

                // Perform the Command.
                return command.onMonoPerform(commandEvent).block();
            });
        }
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
            if (messageChannel.canTalk()) {
                var messageAction = messageChannel.sendMessage(messageCreateData);

                if (deleteSecond > 0) {
                    messageAction.delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                        if (message != null && message.getChannel().retrieveMessageById(message.getId()).complete() != null) {
                            return message.delete();
                        }

                        return null;
                    }).queue();
                    return;
                }

                messageAction.queue();
            }
        } else {
            interactionHook.sendMessage(messageCreateData).queue();
        }
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message        the Message content as {@link Mono<String>}.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(Mono<String> message, MessageChannel messageChannel) {
        message.subscribe(s -> sendMessage(s, messageChannel));
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param message        the Message content as {@link Mono<String>}.
     * @param deleteSecond   the delete delay
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(Mono<String> message, int deleteSecond, MessageChannel messageChannel) {
        message.subscribe(s -> sendMessage(s, deleteSecond, messageChannel));
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message         the Message content as {@link Mono<String>}.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(Mono<String> message, MessageChannel messageChannel, InteractionHook interactionHook) {
        message.subscribe(s -> sendMessage(s, messageChannel, interactionHook));
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param messageContent  the Message content as {@link Mono<String>}.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     * @param deleteSecond    the delete delay
     */
    public void sendMessage(Mono<String> messageContent, int deleteSecond, MessageChannel messageChannel, InteractionHook interactionHook) {
        messageContent.subscribe(s -> sendMessage(s, deleteSecond, messageChannel, interactionHook));
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
                log.error("[CommandManager] Couldn't delete a Message -> {}", throwable.getMessage());
                return null;
            }).queue();
        }
    }

    /**
     * Delete a specific message.
     *
     * @param message         the {@link Message} entity.
     * @param interactionHook the Interaction-hook, if it is a slash event.
     */
    public void deleteMessageWithoutException(Message message, InteractionHook interactionHook) {
        try {
            deleteMessage(message, interactionHook);
        } catch (Exception e) {
            log.error("[CommandManager] Couldn't delete a Message -> {}", e.getMessage());
        }
    }

}