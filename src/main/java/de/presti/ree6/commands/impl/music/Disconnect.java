package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

@Command(name = "disconnect", description = "Disconnect Ree6 from the current Voice-chat!", category = Category.MUSIC)
public class Disconnect implements ICommand {

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

            em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                    commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");
            em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

            Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[] { "dc", "leave" };
    }
}
