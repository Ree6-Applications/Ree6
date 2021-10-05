package de.presti.ree6.commands;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.impl.community.*;
import de.presti.ree6.commands.impl.fun.*;
import de.presti.ree6.commands.impl.hidden.*;
import de.presti.ree6.commands.impl.info.*;
import de.presti.ree6.commands.impl.level.*;
import de.presti.ree6.commands.impl.mod.*;
import de.presti.ree6.commands.impl.music.*;
import de.presti.ree6.commands.impl.nsfw.*;
import de.presti.ree6.main.Main;
import de.presti.ree6.stats.StatsManager;
import de.presti.ree6.utils.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


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

        //NSFW
        addCommand(new NSFW());

        //Hidden
        addCommand(new ReloadAddons());
        //addCommand(new Gamer());
        //addCommand(new Test());

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

    public boolean perform(Member sender, String msg, Message messageSelf, TextChannel m, InteractionHook interactionHook) {

        if (!msg.toLowerCase().startsWith(Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue()))
            return false;

        if(ArrayUtil.commandCooldown.contains(sender.getUser().getId())) {
            sendMessage("You are on cooldown!", 5, m, interactionHook);
            if (interactionHook == null) deleteMessage(messageSelf);
            return false;
        }

        msg = msg.substring(Main.sqlWorker.getSetting(sender.getGuild().getId(), "chatprefix").getStringValue().length());

        String[] oldArgs = msg.split(" ");

        boolean blocked = false;

        for (Command cmd : getCommands()) {
            if (cmd.getCmd().equalsIgnoreCase(oldArgs[0]) || cmd.isAlias(oldArgs[0])) {

                if (!Main.sqlWorker.getSetting(m.getGuild().getId(),"command_" + cmd.getCmd().toLowerCase()).getBooleanValue()) {
                    sendMessage("This Command is blocked!", 5, m, interactionHook);
                    blocked = true;
                    break;
                }

                String[] args = Arrays.copyOfRange(oldArgs, 1, oldArgs.length);
                cmd.onPerform(sender, messageSelf, args, m, interactionHook);
                StatsManager.addStatsForCommand(cmd, m.getGuild().getId());

                if (!sender.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignore) {
                        }

                        ArrayUtil.commandCooldown.remove(sender.getUser().getId());

                        Thread.currentThread().interrupt();

                    }).start();
                }

                if (!ArrayUtil.commandCooldown.contains(sender.getUser().getId()) && !sender.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                    ArrayUtil.commandCooldown.add(sender.getUser().getId());
                }
                return true;
            }
        }

        if (!blocked) {
            sendMessage("That Command couldn't be found", 5, m, interactionHook);
        }

        return false;
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
        if (hook == null) m.sendMessage(msg).queue(); else hook.sendMessage(msg).queue();
    }

    public void sendMessage(String msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessage(msg).delay(deleteSecond, TimeUnit.SECONDS).flatMap(Message::delete).queue(); else hook.sendMessage(msg).delay(deleteSecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }


    public void sendMessage(EmbedBuilder msg, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessageEmbeds(msg.build()).queue(); else hook.sendMessageEmbeds(msg.build()).queue();
    }

    public void sendMessage(EmbedBuilder msg, int deleteSecond, MessageChannel m, InteractionHook hook) {
        if (hook == null) m.sendMessageEmbeds(msg.build()).delay(deleteSecond, TimeUnit.SECONDS).flatMap(Message::delete).queue(); else hook.sendMessageEmbeds(msg.build()).delay(deleteSecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }

    public static void deleteMessage(Message message) {
        if(message != null) {
            if(message.getGuild().getMemberById(BotInfo.botInstance.getSelfUser().getId()).hasPermission(Permission.MESSAGE_MANAGE)) {
            try {
                message.delete().queue();
            } catch (Exception ignore) {
                Logger.log("CommandSystem", "Couldn't delete a Message!");
            }
            } else {
                try {
                    message.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel -> {
                        try {
                            privateChannel.sendMessage("Hey this is just a Message because Ree6 isn't setuped right! Please check if i have the right Permissions or kick and invite me again!").queue();
                        } catch (Exception ignore) {
                        }
                    });
                } catch (Exception ex) {
                    Logger.log("CommandSystem", "Couldn't send a Message to the Server Owner! (GID: " + message.getGuild().getId() + ", OID: " + message.getGuild().getOwner().getId() + ")");
                }
            }
        }
    }

}