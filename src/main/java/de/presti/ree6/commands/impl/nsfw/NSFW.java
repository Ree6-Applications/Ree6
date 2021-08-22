package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.utils.Neko4JsAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import net.dv8tion.jda.api.interactions.InteractionHook;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

import java.util.Random;

public class NSFW extends Command {

    private final String[] tags = new String[] { "solog", "smallboobs", "solo", "cum", "les", "bj", "pwankg", "tits", "nsfw_neko_gif", "cum_jpg", "blowjob", "boobs", "pussy_jpg", "anal", "futanari"};

    public NSFW() {
        super("nsfw", "Get NSFW Images from neko.life", Category.NSFW, new String[] { "givensfw"});
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (m.isNSFW()) {
            ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

            Image im = ip.getRandomImage(tags[new Random().nextInt(tags.length - 1)]).execute();

            EmbedBuilder em = new EmbedBuilder();

            em.setImage((im.getUrl() != null ? im.getUrl() : "https://images.ree6.de/notfound.png"));
            em.setFooter(sender.getUser().getAsTag(), sender.getUser().getAvatarUrl());

            sendMessage(em, m, hook);
        } else {
            sendMessage("Only available in NSFW Channels!", 5, m, hook);
        }
    }
}
