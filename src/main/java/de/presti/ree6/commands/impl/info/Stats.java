package de.presti.ree6.commands.impl.info;

import com.sun.management.OperatingSystemMXBean;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.stats.StatsManager;
import de.presti.ree6.utils.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Stats extends Command {

    public Stats() {
        super("stats", "Show some BotStats!", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {

        long start = System.currentTimeMillis();

        deleteMessage(messageSelf, hook);

        EmbedBuilder em = new EmbedBuilder();

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Stats!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(BotUtil.randomEmbedColor());

        int i = 0;

        for(Guild guild : BotInfo.botInstance.getGuilds()) {
            i += guild.getMemberCount();
        }

        em.addField("**Server Stats:**", "", true);
        em.addField("**Guilds**", BotInfo.botInstance.getGuilds().size() + "", true);
        em.addField("**Users**", i + "", true);

        em.addField("**Bot Stats:**", "", true);
        em.addField("**Version**", BotInfo.build + "-" + BotInfo.version.name().toUpperCase(), true);
        em.addField("**Uptime**", TimeUtil.getTime(BotInfo.startTime), true);

        em.addField("**Network Stats:**", "", true);
        em.addField("**Response Time**", (Integer.parseInt((System.currentTimeMillis() - start) + "")) + "ms", true);
        em.addField("**System Date**" , new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), true);

        if (sender.getId().equalsIgnoreCase("321580743488831490")) {
            em.addField("**Server Stats:**", "", true);
            em.addField("**Ram Usage:**", String.format("%.2f GB / %.2f GB", ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1e+9), (Runtime.getRuntime().maxMemory() / 1e+9)), true);
            em.addField("**CPU Usage:**", String.format("%.2f", ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100) + "%", true);
        }

        StringBuilder end = new StringBuilder();

        for(Map.Entry<String, Long> sheesh : StatsManager.getCommandStats(m.getGuild().getId()).entrySet()) {
            end.append(sheesh.getKey()).append(" - ").append(sheesh.getValue()).append("\n");
        }

        StringBuilder end2 = new StringBuilder();

        for(Map.Entry<String, Long> sheesh : StatsManager.getCommandStats().entrySet()) {
            end2.append(sheesh.getKey()).append(" - ").append(sheesh.getValue()).append("\n");
        }

        em.addField("**Command Stats:**", "", true);
        em.addField("**Top Commands**", end.toString(), true);
        em.addField("**Overall Top Commands**", end2.toString(), true);

        em.setFooter(m.getGuild().getName() + " - " + Data.ADVERTISEMENT, m.getGuild().getIconUrl());

        sendMessage(em, m, hook);

    }
}
