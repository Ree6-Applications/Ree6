package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Disconnect extends CommandClass {

    public Disconnect() {
        super("disconnect", "Disconnect the Bot!", Category.MUSIC, new String[] { "dc", "leave" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()) != null &&
                Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).getSendHandler().isMusicPlaying(commandEvent.getGuild())) {
            if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent) && Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
                return;
            }
            Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.stopAll(commandEvent.getGuild(), commandEvent.getInteractionHook());
        } else {
            EmbedBuilder em = new EmbedBuilder();

            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");
            em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

            sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}
