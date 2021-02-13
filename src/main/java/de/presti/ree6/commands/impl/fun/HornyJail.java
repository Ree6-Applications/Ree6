package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HornyJail extends Command {

    public HornyJail() {
        super("hornyjail", "Put someone in the Hornyjail!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (args.length == 1) {
            if (messageSelf.getMentionedMembers().isEmpty()) {
                sendMessage("No User given!", 5, m);
            } else {
                try {

                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet("https://api.dagpi.xyz/image/jail/?url=" + messageSelf.getMentionedMembers().get(0).getUser().getAvatarUrl());
                    request.setHeader("Authorization", Main.config.getConfig().getString("dagpi.apitoken"));
                    HttpResponse response = httpClient.execute(request);

                    OutputStream outputStream =
                            new FileOutputStream(new File("imageapi/hornyjail/" + sender.getUser().getId() + ".png"));

                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = response.getEntity().getContent().read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    outputStream.close();

                    m.sendMessage(messageSelf.getMentionedMembers().get(0).getAsMention() + " is now in the Hornyjail!").queue();
                    m.sendFile(new File("imageapi/hornyjail/" + sender.getUser().getId() + ".png")).queue(message -> new File("imageapi/hornyjail/" + sender.getUser().getId() + ".png").delete());

                } catch (Exception ex) {
                    sendMessage("Error while putting someone in the Hornyjail!\nError: " + ex.getMessage().replaceAll(Main.config.getConfig().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), 5, m);
                }
            }
        } else {
            sendMessage("Use ree!hornyjail @User", 5, m);
        }
    }
}