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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class Twitter extends Command {

    public Twitter() {
        super("twitter", "Let the Mentioned User Tweet something!", Category.FUN);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if(args.length >= 2) {
            if(messageSelf.getMentionedMembers().isEmpty()) {
                sendMessage("No User given!", m);
            } else {
                try {

                    StringBuilder sb = new StringBuilder();

                    for(int i = 1; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }

                    String name = messageSelf.getMentionedMembers().get(0).getUser().getName().replaceAll("&", "%26").replaceAll(" ", "%20");
                    String text = sb.toString().replaceAll("&","%26").replaceAll(" ", "%20");

                    /*if(name.getBytes("UTF-8") != null) {
                        name = "Little%20PogChamp";
                    }*/

                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet("https://api.dagpi.xyz/image/tweet/?url=" + messageSelf.getMentionedMembers().get(0).getUser().getAvatarUrl() + "&username=" + name + "&text=" + text);
                    request.setHeader("Authorization", Main.config.getConfig().getString("dagpi.apitoken"));
                    HttpResponse response = httpClient.execute(request);

                    OutputStream outputStream =
                            new FileOutputStream(new File("imageapi/twitter/" + sender.getUser().getId() + ".png"));

                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = response.getEntity().getContent().read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    outputStream.close();

                    m.sendFile(new File("imageapi/twitter/" + sender.getUser().getId() + ".png")).queue(message -> new File("imageapi/twitter/" + sender.getUser().getId() + ".png").delete());

                } catch (Exception ex) {
                    sendMessage("Error while creating the Tweet!\nError: " + ex.getMessage().replaceAll(Main.config.getConfig().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), m);
                }
            }
        } else {
            sendMessage("Use ree!twitter @User Yourtexthere", m);
        }
    }
}
