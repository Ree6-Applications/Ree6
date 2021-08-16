package de.presti.ree6.commands;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.impl.community.Rainbow;
import de.presti.ree6.commands.impl.community.TwitchNotifier;
import de.presti.ree6.commands.impl.fun.*;
import de.presti.ree6.commands.impl.hidden.ReloadAddons;
import de.presti.ree6.commands.impl.hidden.Test;
import de.presti.ree6.commands.impl.info.*;
import de.presti.ree6.commands.impl.info.Invite;
import de.presti.ree6.commands.impl.level.Leaderboards;
import de.presti.ree6.commands.impl.level.Level;
import de.presti.ree6.commands.impl.mod.*;
import de.presti.ree6.commands.impl.music.*;
import de.presti.ree6.commands.impl.nsfw.NSFW;
import de.presti.ree6.stats.StatsManager;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class CommandManager {

    static String prefix = "ree!";

    static ArrayList<Command> cmds = new ArrayList<>();

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
        //addCommand(new Test());

    }

    public void addSlashCommand() {

        CommandListUpdateAction listUpdateAction = BotInfo.botInstance.updateCommands();

        for (Command command : getCommands()) {
            if (command.getCategory() == Category.HIDDEN) continue;
            if (command == null) continue;
            listUpdateAction.addCommands(command.getCommandData());
        }

        listUpdateAction.queue();
    }

    public void addCommand(Command c) {
        if (!cmds.contains(c)) {
            cmds.add(c);
        }
    }

    public boolean perform(Member sender, String msg, Message messageSelf, TextChannel m) {

        if (!msg.toLowerCase().startsWith(prefix))
            return false;

        if(ArrayUtil.commandcooldown.contains(sender.getUser().getId())) {
            sendMessage("You are on Cooldown!", 5, m);
            deleteMessage(messageSelf);
            return false;
        }

        msg = msg.substring(prefix.length());

        String[] oldargs = msg.split(" ");

        for (Command cmd : getCommands()) {
            if (cmd.getCmd().equalsIgnoreCase(oldargs[0]) || cmd.isAlias(oldargs[0])) {
                String[] args = Arrays.copyOfRange(oldargs, 1, oldargs.length);
                cmd.onPerform(sender, messageSelf, args, m);
                StatsManager.addStatsForCommand(cmd, m.getGuild().getId());

                if (!sender.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }

                        if (ArrayUtil.commandcooldown.contains(sender.getUser().getId())) {
                            ArrayUtil.commandcooldown.remove(sender.getUser().getId());
                        }

                        Thread.currentThread().interrupt();

                    }).start();
                }

                if (!ArrayUtil.commandcooldown.contains(sender.getUser().getId()) && !sender.getUser().getId().equalsIgnoreCase("321580743488831490")) {
                    ArrayUtil.commandcooldown.add(sender.getUser().getId());
                }
                return true;
            }
        }

        sendMessage("The Command " + oldargs[0] + " couldn't be found!", 5, m);

        return false;
    }

    public void removeCommand(Command c) {
        if (cmds.contains(c)) {
            cmds.remove(c);
        }
    }

    public ArrayList<Command> getCommands() {
        return cmds;
    }

    public static void sendMessage(String msg, MessageChannel m) {
        m.sendMessage(msg).queue();
    }

    public static void sendMessage(String msg, int deletesecond, MessageChannel m) {
        m.sendMessage(msg).delay(deletesecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }

    public static void sendMessage(EmbedBuilder msg, MessageChannel m) {
        m.sendMessage(msg.build()).queue();
    }

    public static void sendMessage(EmbedBuilder msg, int deletesecond, MessageChannel m) {
        m.sendMessage(msg.build()).delay(deletesecond, TimeUnit.SECONDS).flatMap(Message::delete).queue();
    }

    public static void deleteMessage(Message message) {
        if(message != null && message.getContentRaw() != null && message.getId() != null) {
            if(message.getGuild().getMemberById(BotInfo.botInstance.getSelfUser().getId()).hasPermission(Permission.MESSAGE_MANAGE)) {
            try {
                message.delete().queue();
            } catch (Exception ex) {
                Logger.log("CommandSystem", "Couldn't delete a Message!");
            }
            } else {
                message.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel -> {
                    try {
                        privateChannel.sendMessage("Hey this is just a Message because Ree6 isn't setuped right! Please check if i have the right Permissions or reinvited me!").queue();
                    } catch (Exception ex) {}
                });
            }
        }
    }

}