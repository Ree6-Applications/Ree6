package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Neko4JsAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class Kiss extends Command {

    public Kiss() {
        super("kiss", "Kiss someone!", Category.FUN, new CommandData("kiss", "Kiss someone").addOptions(new OptionData(OptionType.USER, "target", "The User that should be kissed!").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (args.length == 1) {
            if(messageSelf.getMentionedMembers().isEmpty()) {
                sendMessage("No User mentioned!", 5, m, hook);
                sendMessage("Use " + Main.sqlConnector.getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "kiss @user", 5, m, hook);
            } else {

                User target = messageSelf.getMentionedMembers().get(0).getUser();

                sendMessage(sender.getAsMention() + " kissed " + target.getAsMention(), m, hook);

                ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

                Image im = null;
                try {
                    im = ip.getRandomImage("kiss").execute();
                } catch (Exception ignored) {
                }

                sendMessage((im != null ? im.getUrl() : "https://images.ree6.de/notfound.png"), m, hook);
            }
        } else {
            sendMessage("Not enough Arguments!", 5, m, hook);
            sendMessage("Use " + Main.sqlConnector.getSqlWorker().getSetting(sender.getGuild().getId(), "chatprefix").getStringValue() + "kiss @user", 5, m, hook);
        }
    }
}
