package de.presti.ree6.commands.impl.community;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import java.awt.*;

public class Rainbow extends Command {

    public Rainbow() {
        super("rainbow", "Search for Rainbow Mates across Server!", Category.COMMUNITY, new String[] { "r6" , "rainbowsixsiege"});
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());

        if (commandEvent.isSlashCommand()) {
            sendMessage("This Command doesn't support slash commands yet.", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        if(!Main.getInstance().getSqlConnector().getSqlWorker().isRainbowSetup(commandEvent.getGuild().getId())) {
            sendMessage("Rainbow Mate searcher isn't setuped!\nAsk a Admin to set it up with "+ Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "setup r6", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return;
        }

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6 R6 Mate searcher!");

        WebhookEmbedBuilder em = new WebhookEmbedBuilder();

        em.setColor(Color.GREEN.getRGB());
        em.setThumbnailUrl(commandEvent.getMember().getUser().getAvatarUrl());
        em.setDescription(commandEvent.getMember().getUser().getName() + " is searching for Rainbow Mates!");
        em.addField(new WebhookEmbed.EmbedField(true, "**Discord Tag**", commandEvent.getMember().getUser().getAsTag()));

        wm.addEmbeds(em.build());

        for(Guild g : BotInfo.botInstance.getGuilds()) {
            if(Main.getInstance().getSqlConnector().getSqlWorker().isRainbowSetup(g.getId())) {
                String[] info = Main.getInstance().getSqlConnector().getSqlWorker().getRainbowWebhook(g.getId());
                Webhook.sendWebhook(null, wm.build(), Long.parseLong(info[0]), info[1]);
            }
        }

    }
}
