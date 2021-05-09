package de.presti.ree6.commands.impl.community;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Rainbow extends Command {

    public Rainbow() {
        super("rainbow", "Search for Rainbow Mates across Server!", Category.COMMUNITY, new String[] { "r6" , "rainbowsixsiege"});
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        deleteMessage(messageSelf);

        if(!Main.sqlWorker.hasRainbowSetuped(m.getGuild().getId())) {
            sendMessage("Rainbow Mate searcher isnt setuped!\nAsk a Admin to set it up with ree!setup r6", 5, m);
            return;
        }

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6 R6 Mate searcher!");

        WebhookEmbedBuilder em = new WebhookEmbedBuilder();

        em.setColor(Color.GREEN.getRGB());
        em.setThumbnailUrl(sender.getUser().getAvatarUrl());
        em.setDescription(sender.getUser().getName() + " is searching for Rainbow Mates!");
        em.addField(new WebhookEmbed.EmbedField(true, "**Discord Tag**", sender.getUser().getAsTag()));

        wm.addEmbeds(em.build());

        for(Guild g : BotInfo.botInstance.getGuilds()) {
            if(Main.sqlWorker.hasRainbowSetuped(g.getId())) {
                String[] info = Main.sqlWorker.getRainbowHooks(g.getId());
                Webhook.sendWebhook(wm.build(), Long.valueOf(info[0]), info[1]);
            }
        }

    }
}
