package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.Neko4JsAPI;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageProvider;

public class Kiss extends CommandClass {

    public Kiss() {
        super("kiss", "Kiss someone!", Category.FUN, new CommandDataImpl("kiss", "Kiss someone")
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should be kissed!").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendKiss(targetOption.getAsMember(), commandEvent);
            } else {
                sendMessage("No User was given to Kiss!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendMessage("No User mentioned!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kiss @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendKiss(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                sendMessage("Not enough Arguments!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "kiss @user", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendKiss(Member member, CommandEvent commandEvent) {

        sendMessage(commandEvent.getMember().getAsMention() + " kissed " + member.getAsMention(), commandEvent.getTextChannel(), null);

        ImageProvider ip = Neko4JsAPI.imageAPI.getImageProvider();

        Image im = null;
        try {
            im = ip.getRandomImage("kiss").execute();
        } catch (Exception ignored) {
        }

        sendMessage((im != null ? im.getUrl() : "https://images.ree6.de/notfound.png"), commandEvent.getTextChannel(), null);
        if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
    }
}
