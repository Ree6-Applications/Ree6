package de.presti.ree6.utils.apis;

import com.lilittlecat.chatgpt.offical.ChatGPT;
import com.lilittlecat.chatgpt.offical.entity.Message;
import com.lilittlecat.chatgpt.offical.exception.BizException;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Data;
import io.sentry.Sentry;
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
     *
     * @param addition the addition.
     */
    public void updatePreDefinedText(String addition) {
        preDefinedInformation += addition;
    }

    /**
     * Method to initialise and create the API Wrapper Instance.
     */
    public void initGPT() {
        if (Data.isModuleActive("ai")) {
            preDefinedInformation = Main.getInstance().getConfig().getConfiguration().getString("bot.misc.predefineInformation");
            chatGPT = new ChatGPT(Main.getInstance().getConfig().getConfiguration().getString("openai.apiUrl", "https://api.openai.com/v1/chat/completions"),
                    Main.getInstance().getConfig().getConfiguration().getString("openai.apiToken"));
        }
    }

    /**
     * Method used to get a response from the AI and storing the message in the in-memory List.
     *
     * @param member  the Member who send it.
     * @param message the Message of the Member.
     * @return the response by the Model.
     */
    public static String getResponse(Member member, String message) {
        String ids = "%s-%s".formatted(member.getGuild().getIdLong(), member.getIdLong());

        List<Message> messages = new ArrayList<>();

        if (ArrayUtil.chatGPTMessages.containsKey(ids)) {
            messages = ArrayUtil.chatGPTMessages.get(ids);
        }

        messages.add(new Message("user", message));

        String response;
        try {
            response = getResponse(messages);
        } catch (BizException e) {
            Sentry.captureException(e);
            return LanguageService.getByGuild(member.getGuild(), "message.default.retrievalError");
        }

        messages.add(new Message("assistant", response));
        ArrayUtil.chatGPTMessages.put(ids, messages);
        return response;
    }

    /**
     * Get a response based on a list of messages.
     *
     * @param messages the messages.
     * @return the response by the Model.
     */
    public static String getResponse(List<Message> messages) {
        if (!Data.isModuleActive("ai")) return Data.shouldHideModuleNotification() ? "" : "AI Module has been disabled!";

        return chatGPT.ask(Main.getInstance().getConfig().getConfiguration().getString("openai.model", "gpt-3.5-turbo-0301"), messages);
    }
}
