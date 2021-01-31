package de.presti.ree6.utils;

import de.presti.ree6.bot.BotInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class ArrayUtil {

    public static HashMap<String, String> messageIDwithMessage = new HashMap<>();
    public static HashMap<String, User> messageIDwithUser = new HashMap<>();
    public static HashMap<Guild, Member> botjoin = new HashMap<>();


    public static User getUserFromMessageList(String id) {
        if(!messageIDwithUser.containsKey(id)) {
            return BotInfo.botInstance.getSelfUser();
        } else {
            return messageIDwithUser.get(id);
        }
    }

    public static String getMessageFromMessageList(String id) {
        if(!messageIDwithMessage.containsKey(id)) {
            return "Nicht Gefunden!";
        } else {
            return messageIDwithMessage.get(id);
        }
    }

    public static void updateMessage(String messageId, String contentRaw) {
        if(messageIDwithMessage.containsKey(messageId)) {
            messageIDwithMessage.remove(messageId);
        }
        messageIDwithMessage.put(messageId, contentRaw);
    }
}