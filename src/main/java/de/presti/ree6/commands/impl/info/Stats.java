package de.presti.ree6.commands.impl.info;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.stats.CommandStats;
import de.presti.ree6.sql.entities.stats.GuildCommandStats;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A command to show you the stats of Ree6.
 */
@Command(name = "stats", description = "command.description.stats", category = Category.INFO)
public class Stats implements ICommand {

    /**
     * @inheritDoc
     */
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

        em.addField("**" + commandEvent.getResource("label.serverStats") + ":**", "", true);
        em.addField("**" + commandEvent.getResource("label.guilds") + "**", BotWorker.getShardManager().getGuilds().size() + "", true);
        em.addField("**" + commandEvent.getResource("label.users") + "**", i + "", true);

        em.addField("**" + commandEvent.getResource("label.botStats") + ":**", "", true);
        em.addField("**" + commandEvent.getResource("label.version") + "**", BotWorker.getBuild() + "-" + BotWorker.getVersion().name().toUpperCase(), true);
        em.addField("**" + commandEvent.getResource("label.uptime") + "**", TimeUtil.getTime(BotWorker.getStartTime()), true);

        em.addField("**" + commandEvent.getResource("label.networkStats") + ":**", "", true);
        em.addField("**" + commandEvent.getResource("label.responseTime") + "**", (Integer.parseInt((System.currentTimeMillis() - start) + "")) + "ms", true);
        em.addField("**" + commandEvent.getResource("label.systemDate") + "**", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), true);

        em.addField("**" + commandEvent.getResource("label.discordStats") + ":**", "", true);
        em.addField("**" + commandEvent.getResource("label.gatewayTime") + "**", BotWorker.getShardManager().getAverageGatewayPing() + "ms", true);
        em.addField("**" + commandEvent.getResource("label.shardAmount") + "**", BotWorker.getShardManager().getShards().size() + " "  + commandEvent.getResource("label.shards"), true);

        StringBuilder end = new StringBuilder();

        for (GuildCommandStats values : Main.getInstance().getSqlConnector().getSqlWorker().getStats(commandEvent.getGuild().getId())) {
            end.append(values.getCommand()).append(" - ").append(values.getUses()).append("\n");
        }

        StringBuilder end2 = new StringBuilder();

        for (CommandStats values : Main.getInstance().getSqlConnector().getSqlWorker().getStatsGlobal()) {
            end2.append(values.getCommand()).append(" - ").append(values.getUses()).append("\n");
        }

        em.addField("**" + commandEvent.getResource("label.commandStats") + ":**", "", true);
        em.addField("**" + commandEvent.getResource("label.topCommands") + "**", end.toString(), true);
        em.addField("**" + commandEvent.getResource("label.overallTopCommands") + "**", end2.toString(), true);

        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        commandEvent.reply(em.build());
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
        return new String[0];
    }
}
