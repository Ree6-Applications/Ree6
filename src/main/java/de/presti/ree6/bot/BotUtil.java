package de.presti.ree6.bot;

import de.presti.ree6.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Random;

public class BotUtil {

    public static void createBot(BotVersion version, String build) throws LoginException {
        BotInfo.version = version;
        BotInfo.TOKEN = FileUtil.getToken();
        BotInfo.state = BotState.INIT;
        BotInfo.build = build;
        BotInfo.botInstance = JDABuilder.createDefault(BotInfo.TOKEN).enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).disableIntents(GatewayIntent.GUILD_PRESENCES).disableIntents(GatewayIntent.DIRECT_MESSAGES).disableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS).disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING).setMemberCachePolicy(MemberCachePolicy.ALL).build();
    }

    public static void setActivity(String message, Activity.ActivityType at) {
        BotInfo.botInstance.getPresence().setActivity(Activity.of(at, message));
    }

    public static void shutdown() {
        if (BotInfo.botInstance != null) {
            BotInfo.botInstance.shutdownNow();
        }
    }

    public static MessageEmbed createEmbed(String author, String description, String footer, String title, String thumb, String image, Color color) {

        EmbedBuilder em = new EmbedBuilder();

        if (!author.isEmpty())
            em.setAuthor(author);

        if (!description.isEmpty())
            em.setDescription(description);

        if (!footer.isEmpty())
            em.setFooter(footer);

        if (!title.isEmpty())
            em.setTitle(title);

        if (!thumb.isEmpty())
            em.setThumbnail(thumb);

        if (color != null)
            em.setColor(color);

        if (!image.isEmpty())
            em.setImage(image);

        return em.build();
    }

    public static void addEvent(ListenerAdapter la) {
        BotInfo.botInstance.addEventListener(la);
    }

    public static Color randomEmbedColor() {
        String zeros = "000000";
        Random rnd = new Random();
        String s = Integer.toString(rnd.nextInt(0X1000000), 16);
        s = zeros.substring(s.length()) + s;
        return Color.decode("#" + s);
    }

}
