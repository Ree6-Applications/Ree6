package de.presti.ree6.commands;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.commands.exceptions.CommandInitializerException;
import de.presti.ree6.commands.impl.community.TwitchNotifier;
import de.presti.ree6.commands.impl.community.TwitterNotifier;
import de.presti.ree6.commands.impl.fun.*;
import de.presti.ree6.commands.impl.hidden.Addon;
import de.presti.ree6.commands.impl.info.Invite;
import de.presti.ree6.commands.impl.info.*;
import de.presti.ree6.commands.impl.level.Leaderboards;
import de.presti.ree6.commands.impl.level.Level;
import de.presti.ree6.commands.impl.mod.*;
import de.presti.ree6.commands.impl.music.*;
import de.presti.ree6.commands.impl.nsfw.NSFW;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.ArrayUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Manager class used to manage all Commands and command related operation.
 */
public class CommandManager {

    // An Arraylist with all registered Commands.
    static final ArrayList<ICommand> commands = new ArrayList<>();

    /**
     * Constructor for the Command-Manager used to register every Command.
     * @throws CommandInitializerException if an error occurs while initializing the Commands.
     */
    public CommandManager() throws CommandInitializerException {
        Main.getInstance().getLogger().info("Initializing Commands!");

        //Informative
        addCommand(new Help());
        addCommand(new Support());
        addCommand(new Info());
        addCommand(new Optout());
        addCommand(new Stats());
        addCommand(new Invite());
        addCommand(new Server());
        addCommand(new Credits());

        //Moderate
        addCommand(new ClearData());
        addCommand(new Prefix());
        addCommand(new Webinterface());
        addCommand(new Clear());
        addCommand(new Setup());
        addCommand(new Mute());
        addCommand(new Unmute());
        addCommand(new Kick());
        addCommand(new Ban());
        addCommand(new Unban());
        addCommand(new ChatProtector());

        //Music
        addCommand(new SongInfo());
        addCommand(new Lyrics());
        addCommand(new Play());
        addCommand(new Pause());
        addCommand(new Resume());
        addCommand(new Stop());
        addCommand(new Disconnect());
        addCommand(new Skip());
        addCommand(new Loop());
        addCommand(new Shuffle());
        addCommand(new Volume());
        addCommand(new Clearqueue());
        addCommand(new SongList());

        //Fun
        addCommand(new RandomAnswer());
        addCommand(new FunFact());
        addCommand(new CatImage());
        addCommand(new DogImage());
        addCommand(new MemeImage());
        addCommand(new Ping());
        addCommand(new Slap());
        addCommand(new Twitter());
        addCommand(new HornyJail());
        addCommand(new Waifu());
        addCommand(new Kiss());
        addCommand(new Hug());
        addCommand(new Cringe());
        addCommand(new FunnyCryptocurrencies());

        //Level
        addCommand(new Level());
        addCommand(new Leaderboards());

        //Community
        addCommand(new TwitchNotifier());
        addCommand(new TwitterNotifier());

        //NSFW
        addCommand(new NSFW());

        //Hidden
        addCommand(new Addon());
        //// addCommand(new Gamer());
        //// addCommand(new Test());
    }

    /**
     * Method used to add all Commands as SlashCommand on Discord.
     *
     * @param jda Instance of the Bot.
     */
    public void addSlashCommand(JDA jda) {
        CommandListUpdateAction listUpdateAction = jda.updateCommands();

        for (ICommand command : getCommands()) {
            Command commandAnnotation = command.getClass().getAnnotation(Command.class);

            if (commandAnnotation.category() == Category.HIDDEN) continue;

            CommandData commandData;

            if (command.getCommandData() != null) {
                commandData = command.getCommandData();
            } else {
                commandData = new CommandDataImpl(command.getClass().getAnnotation(Command.class).name(), command.getClass().getAnnotation(Command.class).description());
            }

            if (commandData != null) {

                if (commandAnnotation.category() == Category.MOD &&
                        commandData.getDefaultPermissions().equals(DefaultMemberPermissions.ENABLED)) {
                    commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
                }
                commandData.setGuildOnly(true);

                //noinspection ResultOfMethodCallIgnored
                listUpdateAction.addCommands(commandData);
            }
        }

        listUpdateAction.queue();
    }

    /**
     * Add a single Command to the Command list.
     *
     * @param command the {@link ICommand}.
     */
    public void addCommand(ICommand command) throws CommandInitializerException {
        if (!command.getClass().isAnnotationPresent(Command.class) || command.getClass().getAnnotation(Command.class).category() == null)
            throw new CommandInitializerException(command.getClass());

        if (!commands.contains(command)) {
            commands.add(command);
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
     * Remove a Command from the List.
     *
     * @param command the Command you want to remove.
     */
    @SuppressWarnings("unused")
    public void removeCommand(ICommand command) {
        commands.remove(command);
    }

    /**
     * Get every Command in the list.
     *
     * @return an {@link ArrayList} with all Commands.
     */
    public ArrayList<ICommand> getCommands() {
        return commands;
    }

    /**
     * Try to perform a Command.
     *
     * @param member                       the Member that performed the try.
     * @param guild                        the Guild the Member is from.
     * @param messageContent               the Message content (including the prefix + command name).
     * @param message                      the Message Entity.
     * @param textChannel                  the TextChannel where the command has been performed.
     * @param slashCommandInteractionEvent the Slash Command Event if it was a Slash Command.
     * @return true, if a command has been performed.
     */
    public boolean perform(Member member, Guild guild, String messageContent, Message message, TextChannel textChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {

        // Check if the User is under Cooldown.
        if (isTimeout(member.getUser())) {

            // Check if it is a Slash Command or not.
            if (slashCommandInteractionEvent != null) {
                sendMessage("You are on cooldown!", 5, textChannel, slashCommandInteractionEvent.getHook().setEphemeral(true));
                deleteMessage(message, slashCommandInteractionEvent.getHook().setEphemeral(true));
            } else if (messageContent.toLowerCase().startsWith(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix").getStringValue().toLowerCase())) {
                sendMessage("You are on cooldown!", 5, textChannel, null);
                deleteMessage(message, null);
            }

            // Return false.
            return false;
        }

        // Check if it is a Slash Command.
        if (slashCommandInteractionEvent != null) {
            if (!performSlashCommand(slashCommandInteractionEvent)) {
                return false;
            }
        } else {
            if (!performMessageCommand(member, guild, messageContent, message, textChannel)) {
                return false;
            }
        }

        // Check if this is a Developer build, if not then cooldown the User.
        if (BotWorker.getVersion() != BotVersion.DEV) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {
                    Main.getInstance().getLogger().error("[CommandManager] Command cool-down Thread interrupted!");
                    Thread.currentThread().interrupt();
                }

                ArrayUtil.commandCooldown.remove(member.getUser().getId());

                Thread.currentThread().interrupt();

            }).start();
        }

        // Add them to the Cooldown.
        if (!ArrayUtil.commandCooldown.contains(member.getUser().getId()) && BotWorker.getVersion() != BotVersion.DEV) {
            ArrayUtil.commandCooldown.add(member.getUser().getId());
        }

        // Return that a command has been performed.
        return true;
    }

    private boolean performMessageCommand(Member member, Guild guild, String messageContent, Message message, TextChannel textChannel) {
        // Check if the Message is null.
        if (message == null) {
            sendMessage("There was an error while executing the Command!", 5, textChannel, null);
            return false;
        }

        // Check if the message starts with the prefix.
        if (!messageContent.toLowerCase().startsWith(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix").getStringValue().toLowerCase()))
            return false;

        // Parse the Message and remove the prefix from it.
        messageContent = messageContent.substring(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix").getStringValue().length());

        // Split all Arguments.
        String[] arguments = messageContent.split(" ");

        // Get the Command by the name.
        ICommand command = getCommandByName(arguments[0]);

        // Check if there is even a Command with that name.
        if (command == null) {
            sendMessage("That Command couldn't be found", 5, textChannel, null);
            return false;
        }

        // Check if the Command is blacklisted.
        if (!Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase()).getBooleanValue() && command.getClass().getAnnotation(Command.class).category() != Category.HIDDEN) {
            sendMessage("This Command is blocked!", 5, textChannel, null);
            return false;
        }

        // Parse the arguments.
        String[] argumentsParsed = Arrays.copyOfRange(arguments, 1, arguments.length);

        // Perform the Command.
        command.onASyncPerform(new CommandEvent(member, guild, message, textChannel, argumentsParsed, null));

        return true;
    }

    /**
     * Call when a slash command has been performed.
     *
     * @param slashCommandInteractionEvent the Slash-Command Event.
     */
    private boolean performSlashCommand(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        //Get the Command by the Slash Command Name.
        ICommand command = getCommandByName(slashCommandInteractionEvent.getName());

        // Check if there is a command with that Name.
        if (command == null || slashCommandInteractionEvent.getGuild() == null || slashCommandInteractionEvent.getMember() == null) {
            sendMessage("That Command couldn't be found", 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
            return false;
        }

        // Check if the command is blocked or not.
        if (!Main.getInstance().getSqlConnector().getSqlWorker().getSetting(slashCommandInteractionEvent.getGuild().getId(), "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase()).getBooleanValue() && command.getClass().getAnnotation(Command.class).category() != Category.HIDDEN) {
            sendMessage("This Command is blocked!", 5, null, slashCommandInteractionEvent.getHook().setEphemeral(true));
            return false;
        }

        // Perform the Command.
        command.onASyncPerform(new CommandEvent(slashCommandInteractionEvent.getMember(), slashCommandInteractionEvent.getGuild(), null, null, null, slashCommandInteractionEvent));

        return true;
    }

    public boolean isTimeout(User user) {
        return ArrayUtil.commandCooldown.contains(user.getId()) && BotWorker.getVersion() != BotVersion.DEV;
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message        the Message content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(String message, MessageChannel messageChannel) {
        if (messageChannel.canTalk()) sendMessage(message, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel, with a deletion delay.
     *
     * @param message        the Message content.
     * @param deleteSecond   the delete delay
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(String message, int deleteSecond, MessageChannel messageChannel) {
        if (messageChannel.canTalk()) sendMessage(message, deleteSecond, messageChannel, null);
    }

    /**
     * Send a message to a special Message-Channel.
     *
     * @param message         the Message content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(String message, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel.canTalk()) messageChannel.sendMessage(message).queue();
        } else interactionHook.sendMessage(message).queue();
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
        if (interactionHook == null) {
            if (messageChannel == null) return;
            if (messageChannel.canTalk())
                messageChannel.sendMessage(messageContent).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                    if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                        return message.delete();
                    }

                    return null;
                }).queue();
        } else {
            interactionHook.sendMessage(messageContent).queue();
        }
    }

    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder   the Embed content.
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel) {
        if (messageChannel.canTalk()) sendMessage(embedBuilder, messageChannel, null);
    }

    /**
     * Send an Embed to a special Message-Channel, with a deletion delay.
     *
     * @param embedBuilder   the Embed content.
     * @param deleteSecond   the delete delay
     * @param messageChannel the Message-Channel.
     */
    public void sendMessage(EmbedBuilder embedBuilder, int deleteSecond, MessageChannel messageChannel) {
        if (messageChannel.canTalk()) sendMessage(embedBuilder, deleteSecond, messageChannel, null);
    }

    /**
     * Send an Embed to a special Message-Channel.
     *
     * @param embedBuilder    the Embed content.
     * @param messageChannel  the Message-Channel.
     * @param interactionHook the Interaction-hook if it is a slash command.
     */
    public void sendMessage(EmbedBuilder embedBuilder, MessageChannel messageChannel, InteractionHook interactionHook) {
        if (interactionHook == null) {
            if (messageChannel.canTalk()) messageChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        } else interactionHook.sendMessageEmbeds(embedBuilder.build()).queue();
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
        if (interactionHook == null) {
            if (messageChannel == null) return;
            if (messageChannel.canTalk())
                messageChannel.sendMessageEmbeds(embedBuilder.build()).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                    if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                        return message.delete();
                    }

                    return null;
                }).queue();
        } else {
            interactionHook.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    /**
     * Delete a specific message.
     *
     * @param message         the {@link Message} entity.
     * @param interactionHook the Interaction-hook, if it is a slash event.
     */
    public void deleteMessage(Message message, InteractionHook interactionHook) {
        if (message != null && message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) && message.getTextChannel().retrieveMessageById(message.getIdLong()).complete() != null && !message.isEphemeral() && interactionHook == null) {
            try {
                message.delete().queue();
            } catch (Exception ex) {
                Main.getInstance().getLogger().error("[CommandManager] Couldn't delete a Message!");
            }
        }
    }

}