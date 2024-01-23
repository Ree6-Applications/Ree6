package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.bot.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * Disconnects the Bot from the VoiceChannel.
 */
@Command(name = "disconnect", description = "command.description.disconnect", category = Category.MUSIC)
public class Disconnect implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()) != null &&
                Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
                return;
            }
            Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).getScheduler().stopAll(commandEvent.getInteractionHook());
        } else {
            EmbedBuilder em = new EmbedBuilder();

            em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                    commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
            em.setTitle(commandEvent.getResource("label.musicPlayer"));
            em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription(commandEvent.getResource("message.music.notPlaying"));
            em.setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl());

            commandEvent.reply(em.build(), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "dc", "leave" };
    }
}
