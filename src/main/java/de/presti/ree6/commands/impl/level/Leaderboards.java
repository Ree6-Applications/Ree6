package de.presti.ree6.commands.impl.level;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;

public class Leaderboards extends Command {

    public Leaderboards() {
        super("leaderboard", "Shows you the Rank Leaderboard", Category.LEVEL, new String[]{ "lb" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        sendMessage("For the Voice Leaderboard: <https://cp.ree6.de/leaderboard/voice?guildId=" + m.getGuild().getId() + ">\nAnd for the Chat Leaderboard: <https://cp.ree6.de/leaderboard/chat?guildId=" + m.getGuild().getId() + ">", m, hook);
    }
}
