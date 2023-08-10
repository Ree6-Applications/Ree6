package de.presti.ree6.commands.impl.music;

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
 * Play a Song.
 */
@Command(name = "play", description = "command.description.play", category = Category.MUSIC)
public class Play implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        if (commandEvent.isSlashCommand()) {

            OptionMapping valueOption = commandEvent.getOption("name");

            if (valueOption != null) {
                Main.getInstance().getMusicWorker().playSong(valueOption.getAsString(), commandEvent);
            } else {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription(commandEvent.getResource("message.default.usage","play (Url)"));
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());
                commandEvent.reply(em.build(), 5);
            }

        } else {
            if (commandEvent.getArguments().length < 1) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                        commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setTitle(commandEvent.getResource("label.musicPlayer"));
                em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription(commandEvent.getResource("message.default.usage","play (Url)"));
                em.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());
                commandEvent.reply(em.build(), 5);
            } else {
                Main.getInstance().getMusicWorker().playSong(commandEvent.getArguments()[0], commandEvent);
            }
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("play", LanguageService.getDefault("command.description.play")).addOptions(new OptionData(OptionType.STRING, "name", "The YouTube URL, Song Name or the Spotify URL you want to play!").setRequired(true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"p", "music"};
    }
}
