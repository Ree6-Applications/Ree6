package de.presti.ree6.commands.impl.info;

import com.sun.management.OperatingSystemMXBean;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.stats.StatsManager;
import de.presti.ree6.utils.others.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Command(name = "stats", description = "See Stats of Ree6!", category = Category.INFO)
public class Stats implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {

        long start = System.currentTimeMillis();

        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());

        EmbedBuilder em = new EmbedBuilder();

        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setTitle("Stats!");
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setColor(BotWorker.randomEmbedColor());

        int i = 0;

        for (Guild guild : BotWorker.getShardManager().getGuilds()) {
            i += guild.getMemberCount();
        }

        em.addField("**Server Stats:**", "", true);
        em.addField("**Guilds**", BotWorker.getShardManager().getGuilds().size() + "", true);
        em.addField("**Users**", i + "", true);

        em.addField("**Bot Stats:**", "", true);
        em.addField("**Version**", BotWorker.getBuild() + "-" + BotWorker.getVersion().name().toUpperCase(), true);
        em.addField("**Uptime**", TimeUtil.getTime(BotWorker.getStartTime()), true);

        em.addField("**Network Stats:**", "", true);
        em.addField("**Response Time**", (Integer.parseInt((System.currentTimeMillis() - start) + "")) + "ms", true);
        em.addField("**System Date**", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), true);

        StringBuilder end = new StringBuilder();

        for (Map.Entry<String, Long> sheesh : StatsManager.getCommandStats(commandEvent.getGuild().getId()).entrySet()) {
            end.append(sheesh.getKey()).append(" - ").append(sheesh.getValue()).append("\n");
        }

        StringBuilder end2 = new StringBuilder();

        for (Map.Entry<String, Long> sheesh : StatsManager.getCommandStats().entrySet()) {
            end2.append(sheesh.getKey()).append(" - ").append(sheesh.getValue()).append("\n");
        }

        em.addField("**Command Stats:**", "", true);
        em.addField("**Top Commands**", end.toString(), true);
        em.addField("**Overall Top Commands**", end2.toString(), true);

        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
