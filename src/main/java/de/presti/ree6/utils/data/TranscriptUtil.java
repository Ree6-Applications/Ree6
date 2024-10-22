package de.presti.ree6.utils.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class TranscriptUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     */
    private TranscriptUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The HTML template that should be used for the transcripts.
     */
    public static final String template = """
           <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name = "viewport" content="width=device-width, initial-scale=1.0">
                    <script src = "https://cdn.tailwindcss.com"></script>
                </head>
                <body>
                    <style>
                        body { background-color: #202225; color: #dcddde; }
                        .messages-container { margin: 10px; }
                        .message-breaker { margin-top: 15px; }
                        .name {font-size: 16px;}
                        .date {font-weight: normal; margin-left: 5px; color: #a3a6aa; font-size: 12px;}
                    </style>
                    <div>
                        <div class="flex">
                            <div class="flex-none">
                                <img class="rounded-full mr-3" src="%icon%">
                            </div>
                            <div class="flex-initial">
                                <div> Opened: <span class="font-bold">%opened%</span> </div>
                                <div> Transcript Generated:  <span class="font-bold">%closed%</span> </div>
                                <div> Total Messages: <span class="font-bold">%messages_count%</span> </div>
                            </div>
                        </div>
                        <div class="messages-container">
                            %messages%
                        </div>
                    </div>
                </body>
           </html>
           """;

    /**
     * The Message template to be inserted into the HTML template.
     */
    public static final String messageTemplate = """
            <div class="message-breaker"/>
            <div class="name font-bold">%name%<span class="date">%time%</span> </div>
            <div class="message">
               %message%
            </div>
            """;

    /**
     * Generates a Transcript based on the templates.
     * @param selfUser The Bot Self User to use as an Icon.
     * @param messages The messages that should be put into the transcript.
     * @param opened When the Ticket was opened.
     * @param closed When the Ticket was closed.
     * @return A filled-out template as {@link String}
     */
    public static String generateTranscript(JDA selfUser, List<Message> messages, String opened, String closed) {
        String icon = selfUser.getSelfUser().getEffectiveAvatarUrl();
        StringBuilder messageBuilder = new StringBuilder();
        for (Message message : messages) {
            messageBuilder.append(messageTemplate
                    .replace("%name%", message.getAuthor().getName())
                    .replace("%time%", message.getTimeCreated().toString())
                    .replace("%message%", message.getContentRaw()));
        }

        return template
                .replace("%icon%", icon)
                .replace("%opened%", opened)
                .replace("%closed%", closed)
                .replace("%messages_count%", String.valueOf(messages.size()))
                .replace("%messages%", messageBuilder.toString());
    }

}
