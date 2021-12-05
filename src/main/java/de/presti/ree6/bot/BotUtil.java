package de.presti.ree6.bot;

import de.presti.ree6.main.Data;
import de.presti.ree6.utils.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Random;

/**
 * Class used to cover small utilities for the Bot.
 */
public class BotUtil {

    /**
     * Create a new {@link net.dv8tion.jda.api.JDA} instance and set the rest information for later use.
     * @param version the current Bot Version "typ".
     * @param build the current Bot Version.
     * @throws LoginException when there is a problem with creating the Session.
     */
    public static void createBot(BotVersion version, String build) throws LoginException {
        BotInfo.version = version;
        BotInfo.TOKEN = FileUtil.getToken();
        BotInfo.state = BotState.INIT;
        BotInfo.build = build;
        BotInfo.botInstance = JDABuilder.createDefault(BotInfo.TOKEN).enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).disableIntents(GatewayIntent.GUILD_PRESENCES).disableIntents(GatewayIntent.DIRECT_MESSAGES).disableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS).disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY).enableCache(CacheFlag.VOICE_STATE).build();
    }

    /**
     * Change the current Activity of the Bot.
     * @param message the Message of the Activity.
     * @param at the Activity type.
     */
    public static void setActivity(String message, Activity.ActivityType at) {
        // If the Bot Instance is null, if not set.
        if (BotInfo.botInstance != null) BotInfo.botInstance.getPresence().setActivity(Activity.of(at, message));
    }

    /**
     * Called when the Bot should Shut down.
     */
    public static void shutdown() {
        // Check if the Instance of null if not, shutdown.
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
            em.setFooter(footer + " - " + Data.advertisement);

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
