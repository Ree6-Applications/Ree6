package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.Neko4JsAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class NSFWSearch extends Command {

    public NSFWSearch() {
        super("nsfwsearch", "Search for NSFW on neko.life", Category.NSFW, new String[] { "searchnsfw", "nsfw"});
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (m.isNSFW()) {

            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

            Image im = ip.getRandomImage(sb.toString()).execute();

            EmbedBuilder em = new EmbedBuilder();

            em.setDescription("Here you go!");
            em.setImage(im.getUrl());
            em.setFooter(sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

            sendMessage(em, m);
        } else {
            sendMessage("You only can use this in a NSFW Channel", 5, m);
        }
    }
}
