package de.presti.ree6.utils.apis;

import com.lilittlecat.chatgpt.offical.ChatGPT;
import com.lilittlecat.chatgpt.offical.entity.Message;
import com.lilittlecat.chatgpt.offical.entity.Model;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.ArrayUtil;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * API Handler class for the ChatGPT API Wrapper.
 */
public class ChatGPTAPI {

    /**
     * Instance of the ChatGPT API.
     */
    public static ChatGPT chatGPT;

    // TODO:: add more information about the commands and their syntax.

    /**
     * Pre-defined Text used to inform the AI with information related to Ree6s personality and limitations.
     * This also contains the joke of Ree6 being the Girlfriend of a User on Twitter called @KarmaIsLame.
     */
    static String preDefinedInformation = "";

    /**
     * Constructor to initialise.
     */
    public ChatGPTAPI() {
        initGPT();
    }

    /**
     * Method used to add more information to the predefined text.
     * @param addition the addition.
     */
    public void updatePreDefinedText(String addition) {
        preDefinedInformation += addition;
    }

    /**
     * Method to initialise and create the API Wrapper Instance.
     */
    public void initGPT() {
        preDefinedInformation = Main.getInstance().getConfig().getConfiguration().getString("bot.misc.predefineInformation");
        chatGPT = ChatGPT.builder().apiKey(Main.getInstance().getConfig().getConfiguration().getString("openai.apitoken")).build();
    }

    /**
     * Method used to get a response from the AI and storing the message in the in-memory List.
     * @param member the Member who send it.
     * @param message the Message of the Member.
     * @return the response by the Model.
     */
    public static String getResponse(Member member, String message) {
        long[] ids = new long[] { member.getGuild().getIdLong(), member.getIdLong() };

        List<Message> messages = new ArrayList<>();

        if (ArrayUtil.chatGPTMessages.containsKey(ids)) {
            messages = ArrayUtil.chatGPTMessages.get(ids);
        }

        messages.add(new Message("user", message));

        ArrayUtil.chatGPTMessages.put(ids, messages);
        return getResponse(messages);
    }

    /**
     * Get a response based on a list of messages.
     * @param messages the messages.
     * @return the response by the Model.
     */
    public static String getResponse(List<Message> messages) {
        if (messages.isEmpty() || !messages.get(0).content.equals(preDefinedInformation)) {
            List<Message> newMessageList = new ArrayList<>();
            newMessageList.add(new Message("system", preDefinedInformation));
            newMessageList.addAll(messages);
            messages = newMessageList;
        }

        return chatGPT.ask(Model.GPT_3_5_TURBO, messages);
    }
}
