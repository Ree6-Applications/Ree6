package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.awt.*;

/**
 * Set the Volume of the AudioPlayer of Ree6.
 */
@Command(name = "volume", description = "command.description.volume", category = Category.MUSIC)
public class Volume implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            commandEvent.reply(commandEvent.getResource("message.music.notConnected"));
            return;
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        EmbedBuilder em = new EmbedBuilder();

        // Preloading it so we can use it later.
        AudioPlayer player = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).getPlayer();

        if (commandEvent.isSlashCommand()) {

            OptionMapping volumeOption = commandEvent.getOption("amount");

            if (volumeOption != null) {
                int volume = (int)volumeOption.getAsDouble();

                player.setVolume(volume);

                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription(commandEvent.getResource("message.music.volume.success", volume));
            } else {
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.RED);
                em.setDescription(commandEvent.getResource("message.music.volume.default"));
            }

        } else {

            if (commandEvent.getArguments().length == 1) {
                int vol;

                try {
                    vol = Integer.parseInt(commandEvent.getArguments()[0]);
                } catch (Exception e) {
                    vol = 50;
                }

                player.setVolume(vol);

                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription(commandEvent.getResource("message.music.volume.success", vol));
            } else {
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription(commandEvent.getResource("message.music.volume.default", player.getVolume()));
            }
        }

        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());

        commandEvent.reply(em.build(), 5);
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("volume", LanguageService.getDefault("command.description.volume")).addOptions(new OptionData(OptionType.INTEGER, "amount", "The Volume that the Ree6 Music Player should be!").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "vol" };
    }
}