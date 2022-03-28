package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;

@Command(name = "volume", description = "Change the current Volume.", category = Category.MUSIC)
public class Volume implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            Main.getInstance().getCommandManager().sendMessage("Im not connected to any Channel, so there is nothing to set the volume for!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        if (!Main.getInstance().getMusicWorker().checkInteractPermission(commandEvent)) {
            return;
        }

        EmbedBuilder em = new EmbedBuilder();

        if (commandEvent.isSlashCommand()) {

            OptionMapping volumeOption = commandEvent.getSlashCommandInteractionEvent().getOption("amount");

            if (volumeOption != null) {
                int volume = (int)volumeOption.getAsDouble();

                Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.setVolume(volume);

                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Volume has been set to " + volume);
            } else {
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.RED);
                em.setDescription("No Volume was given.");
            }

        } else {

            if (commandEvent.getArguments().length == 1) {
                int vol;

                try {
                    vol = Integer.parseInt(commandEvent.getArguments()[0]);
                } catch (Exception e) {
                    vol = 50;
                }

                Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).player.setVolume(vol);

                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Volume has been set to " + vol);


            } else {
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Type " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "volume [voulume]");
            }
        }

        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("volume", "Set the Volume!").addOptions(new OptionData(OptionType.INTEGER, "amount", "The Volume that the Ree6 Music Player should be!").setRequired(true));
    }

    @Override
    public String[] getAlias() {
        return new String[] { "vol" };
    }
}