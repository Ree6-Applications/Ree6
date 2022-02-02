package de.presti.ree6.commands;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.impl.community.*;
import de.presti.ree6.commands.impl.fun.*;
import de.presti.ree6.commands.impl.hidden.*;
import de.presti.ree6.commands.impl.info.*;
import de.presti.ree6.commands.impl.info.Invite;
import de.presti.ree6.commands.impl.level.*;
import de.presti.ree6.commands.impl.mod.*;
import de.presti.ree6.commands.impl.music.*;
import de.presti.ree6.commands.impl.nsfw.*;
import de.presti.ree6.main.Main;
import de.presti.ree6.stats.StatsManager;
import de.presti.ree6.utils.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// TODO document.
public class CommandManager {

    static final ArrayList<Command> commands = new ArrayList<>();

    public CommandManager() {

        //Informative
        addCommand(new Help());
        addCommand(new Support());
        addCommand(new Info());
        addCommand(new Stats());
        addCommand(new Invite());
        addCommand(new Server());
        addCommand(new Credits());

        //Moderate
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
        addCommand(new Songinfo());
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
        addCommand(new Songlist());

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
        addCommand(new DogeCoin());

        //Level
        addCommand(new Level());
        addCommand(new Leaderboards());

        //Community
        addCommand(new Rainbow());
        addCommand(new TwitchNotifier());
        addCommand(new TwitterNotifier());

        //NSFW
        addCommand(new NSFW());

        //Hidden
        addCommand(new ReloadAddons());
        //// addCommand(new Gamer());
        //// addCommand(new Test());
    }

    public void addSlashCommand() {

        CommandListUpdateAction listUpdateAction = BotInfo.botInstance.updateCommands();

        for (Command command : getCommands()) {
            if (command.getCategory() == Category.HIDDEN) continue;
            if (command.getCommandData() == null) continue;
            //noinspection ResultOfMethodCallIgnored
            listUpdateAction.addCommands(command.getCommandData());
        }

        listUpdateAction.queue();
    }

    public void addCommand(Command c) {
        if (!commands.contains(c)) {
            commands.add(c);
        }
    }

    public Command getCommandByName(String name) {
        return getCommands().stream().filter(command -> command.getName().equalsIgnoreCase(name) ||
                Arrays.stream(command.getAlias()).anyMatch(s -> s.equalsIgnoreCase(name))).findFirst().orElse(null);

    }

    public boolean perform(Member member, Guild guild, String messageContent, Message message, TextChannel textChannel, SlashCommandInteractionEvent slashCommandInteractionEvent) {


        if (ArrayUtil.commandCooldown.contains(member.getUser().getId())) {

            if (slashCommandInteractionEvent != null) {
                sendMessage("You are on cooldown!", 5, textChannel, slashCommandInteractionEvent.getHook().setEphemeral(true));
                deleteMessage(message, slashCommandInteractionEvent.getHook().setEphemeral(true));
            } else {
                sendMessage("You are on cooldown!", 5, textChannel, null);
                deleteMessage(message, null);
            }

            return false;
        }

        if (slashCommandInteractionEvent != null) {

            Command command = getCommandByName(slashCommandInteractionEvent.getName());


            if (command == null) {
                sendMessage("That Command couldn't be found", 5, textChannel, slashCommandInteractionEvent.getHook().setEphemeral(true));
                return false;
            }

            if (!Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "command_" + command.getName().toLowerCase()).getBooleanValue() &&
                    command.getCategory() != Category.HIDDEN) {
                sendMessage("This Command is blocked!", 5, textChannel, slashCommandInteractionEvent.getHook().setEphemeral(true));
                return false;
            }

            command.onPerform(new CommandEvent(member, guild, null, textChannel, null, slashCommandInteractionEvent));

            StatsManager.addStatsForCommand(command, guild.getId());

            if (!member.getUser().getId().equalsIgnoreCase("321580743488831490")) {
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

            if (!ArrayUtil.commandCooldown.contains(member.getUser().getId()) && !member.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                ArrayUtil.commandCooldown.add(member.getUser().getId());
            }

            return true;

        } else {

            if (message == null) {
                sendMessage("There was an Error while executing the Command!", 5, textChannel, null);
                return false;
            }

            if (!messageContent.toLowerCase().startsWith(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix").getStringValue()))
                return false;

            messageContent = messageContent.substring(Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "chatprefix").getStringValue().length());

            String[] arguments = messageContent.split(" ");

            Command command = getCommandByName(arguments[0]);

            if (command == null) {
                sendMessage("That Command couldn't be found", 5, textChannel, null);
                return false;
            }

            if (!Main.getInstance().getSqlConnector().getSqlWorker().getSetting(guild.getId(), "command_" + command.getName().toLowerCase()).getBooleanValue() &&
                    command.getCategory() != Category.HIDDEN) {
                sendMessage("This Command is blocked!", 5, textChannel, null);
                return false;
            }

            String[] argumentsParsed = Arrays.copyOfRange(arguments, 1, arguments.length);

            command.onPerform(new CommandEvent(member, guild, message, textChannel, argumentsParsed, null));

            StatsManager.addStatsForCommand(command, guild.getId());

            if (!member.getUser().getId().equalsIgnoreCase("321580743488831490")) {
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

            if (!ArrayUtil.commandCooldown.contains(member.getUser().getId()) && !member.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                ArrayUtil.commandCooldown.add(member.getUser().getId());
            }
            return true;
        }
    }

    @SuppressWarnings("unused")
    public void removeCommand(Command c) {
        commands.remove(c);
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public void sendMessage(String msg, MessageChannel m) {
        sendMessage(msg, m, null);
    }

    public void sendMessage(String msg, int deleteSecond, MessageChannel m) {
        sendMessage(msg, deleteSecond, m, null);
    }


    public void sendMessage(EmbedBuilder msg, MessageChannel m) {
        sendMessage(msg, m, null);
    }

    public void sendMessage(EmbedBuilder msg, int deleteSecond, MessageChannel m) {
        sendMessage(msg, deleteSecond, m, null);
    }

    public void sendMessage(String msg, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessage(msg).queue();
        else hook.sendMessage(msg).queue();
    }

    public void sendMessage(String msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        if (hook == null) {
            m.sendMessage(msg).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                    return message.delete();
                }

                return null;
            }).queue();
        } else {
            hook.sendMessage(msg).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                    return message.delete();
                }

                return null;
            }).queue();
        }
    }


    public void sendMessage(EmbedBuilder msg, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessageEmbeds(msg.build()).queue();
        else hook.sendMessageEmbeds(msg.build()).queue();
    }

    public void sendMessage(EmbedBuilder msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        if (hook == null) {
            m.sendMessageEmbeds(msg.build()).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                    return message.delete();
                }

                return null;
            }).queue();
        } else {
            hook.sendMessageEmbeds(msg.build()).delay(deleteSecond, TimeUnit.SECONDS).flatMap(message -> {
                if (message != null && message.getTextChannel().retrieveMessageById(message.getId()).complete() != null) {
                    return message.delete();
                }

                return null;
            }).queue();
        }
    }

    public void deleteMessage(Message message, InteractionHook hook) {
        if (message != null && message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) &&
                message.getTextChannel().retrieveMessageById(message.getIdLong()).complete() != null && !message.isEphemeral() && hook == null) {
            try {
                message.delete().queue();
            } catch (Exception ex) {
                Main.getInstance().getLogger().error("[CommandManager] Couldn't delete a Message!");
            }
        }
    }

}