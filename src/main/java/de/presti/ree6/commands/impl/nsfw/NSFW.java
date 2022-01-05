package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.Neko4JsAPI;
import de.presti.ree6.utils.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class NSFW extends Command {

    private final String[] tags = new String[]{"cum", "tits", "nsfw_neko_gif", "blowjob", "boobs", "anal"};

    public NSFW() {
        super("nsfw", "Get NSFW Images from neko.life", Category.NSFW, new String[]{"givensfw", "hentai"});
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getTextChannel().isNSFW()) {
            ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

            String usedTag = tags[RandomUtils.random.nextInt(tags.length - 1)];

            Image im = ip.getRandomImage(usedTag).execute();

            EmbedBuilder em = new EmbedBuilder();

            em.setImage(im != null && !im.getUrl().isEmpty() ? im.getUrl() : "https://images.ree6.de/notfound.png");
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " Tag: " + usedTag + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

            sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else {
            sendMessage("Only available in NSFW Channels!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}
