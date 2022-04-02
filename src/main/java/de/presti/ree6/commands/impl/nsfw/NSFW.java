package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.Neko4JsAPI;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "nsfw", description = "Get NSFW Image for nekos.life", category = Category.NSFW)
public class NSFW implements ICommand {

    String[] imageTags = new String[] { "Random_hentai_gif", "hentai"};

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getTextChannel().isNSFW()) {
            Neko4JsAPI.imageAPI.getImageProvider().getRandomImage(imageTags[RandomUtils.random.nextInt(imageTags.length)]).async(image -> Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                    .setImage(image.getUrl())
                    .setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl()), commandEvent.getTextChannel(), commandEvent.getInteractionHook()));
        } else {
            Main.getInstance().getCommandManager().sendMessage("Only available in NSFW Channels!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[]{"givensfw", "hentai"};
    }
}
