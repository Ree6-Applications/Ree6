package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
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

    private final String[] tags = new String[]{"cum", "tits", "nsfw_neko_gif", "blowjob", "boobs", "anal"};

    public NSFW() {
        super("nsfw", "Get NSFW Images from neko.life", Category.NSFW, new String[]{"givensfw", "hentai"});
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (m.isNSFW()) {
            ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

            String usedTag = tags[new Random().nextInt(tags.length - 1)];

            Image im = ip.getRandomImage(usedTag).execute();

            EmbedBuilder em = new EmbedBuilder();

            em.setImage(im != null && !im.getUrl().isEmpty() ? im.getUrl() : "https://images.ree6.de/notfound.png");
            em.setFooter(sender.getUser().getAsTag() + " Tag: " + usedTag + " - " + Data.advertisement, sender.getUser().getAvatarUrl());

            sendMessage(em, m, hook);
        } else {
            sendMessage("Only available in NSFW Channels!", 5, m, hook);
        }
    }
}
