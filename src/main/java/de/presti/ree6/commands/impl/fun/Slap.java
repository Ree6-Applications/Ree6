package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Neko4JsAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class Slap extends Command {

    public Slap() {
        super("slap", "Slap someone in the face!", Category.FUN, new CommandDataImpl("slap", "Slap someone in the face!")
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should be slapped!").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendSlap(targetOption.getAsMember(), commandEvent);
            } else {
                sendMessage("No User was given to Slap!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "slap @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendSlap(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "slap @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendSlap(Member member, CommandEvent commandEvent) {
        sendMessage(commandEvent.getMember().getAsMention() + " slapped " + member.getAsMention(), commandEvent.getTextChannel(), null);

        ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

        Image im = null;
        try {
            im = ip.getRandomImage("slap").execute();
        } catch (Exception ignored) {
        }

        sendMessage((im != null ? im.getUrl() : "https://images.ree6.de/notfound.png"), commandEvent.getTextChannel(), null);
        if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
    }
}
