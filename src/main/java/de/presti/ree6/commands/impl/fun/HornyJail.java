package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HornyJail extends Command {

    public HornyJail() {
        super("hornyjail", "Put someone in the Hornyjail!", Category.FUN, new String[]{"horny", "jail"}, new CommandData("hornyjail", "Put someone in the Hornyjail!")
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should be put into the Hornyjail!").setRequired(true)));
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandEvent().getOption("target");

            if (targetOption != null) {
                sendHornyJail(targetOption.getAsMember(), commandEvent);
            } else {
                sendMessage("No User was given to put into the Hornyjail!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    sendMessage("No User given!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendHornyJail(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "hornyjail @User", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    public void sendHornyJail(Member member, CommandEvent commandEvent) {
        try {

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.dagpi.xyz/image/jail/?url=" + member.getUser().getAvatarUrl());
            request.setHeader("Authorization", Main.getInstance().getConfig().getConfig().getString("dagpi.apitoken"));
            HttpResponse response = httpClient.execute(request);

            try (OutputStream outputStream =
                         new FileOutputStream("imageapi/hornyjail/" + commandEvent.getMember().getId() + ".png")) {

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = response.getEntity().getContent().read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }

            sendMessage(member.getAsMention() + " is now in the Hornyjail!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            commandEvent.getTextChannel().sendFile(new File("imageapi/hornyjail/" + commandEvent.getMember().getId() + ".png")).queue(message -> new File("imageapi/hornyjail/" + commandEvent.getMember().getId() + ".png").delete());
            if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        } catch (Exception ex) {
            sendMessage("Error while putting someone in the Hornyjail!\nError: " + ex.getMessage().replaceAll(Main.getInstance().getConfig().getConfig().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}