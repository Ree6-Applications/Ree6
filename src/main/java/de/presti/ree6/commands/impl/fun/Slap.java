package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.Neko4JsAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class Slap extends Command {

    public Slap() {
        super("slap", "Slap someone in the face!", Category.FUN, new CommandData("slap", "Slap someone in the face!").addOptions(new OptionData(OptionType.USER, "target", "The User that should be slapped!").setRequired(true)));
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (args.length == 1) {
            if (messageSelf.getMentionedMembers().isEmpty()) {
                sendMessage("No User mentioned!", 5, m);
                sendMessage("Use ree!slap @user", 5, m);
            } else {

                User target = messageSelf.getMentionedMembers().get(0).getUser();

                sendMessage(sender.getAsMention() + " slapped " + target.getAsMention(), m);

                ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

                Image im = null;
                try {
                    im = ip.getRandomImage("slap").execute();
                } catch (Exception ex) {
                }

                sendMessage((im != null ? im.getUrl() : "https://images.ree6.de/notfound.png"), m);
            }
        } else {
            sendMessage("Not enough Arguments!", m);
            sendMessage("Use ree!slap @user", m);
        }
    }
}
