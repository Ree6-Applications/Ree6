package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

public class Cringe extends Command {


    public Cringe() {
        super("cringe", "Tell someone that his message is CRINGE", Category.LEVEL);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        MessageHistory mh = m.getHistoryBefore(messageSelf.getId(), 1).complete();

        sendMessage("```" + mh.getRetrievedHistory().get(0).getContentRaw() + "``` damn " + mh.getRetrievedHistory().get(0).getAuthor().getAsMention() + " thats **CRINGE**", m);

        deleteMessage(messageSelf);
    }
}
