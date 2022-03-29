package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Command(name = "twitter", description = "Create a \"realistic\" looking Tweet!", category = Category.FUN)
public class Twitter implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");
            OptionMapping contentOption = commandEvent.getSlashCommandInteractionEvent().getOption("content");

            if (targetOption != null && contentOption != null) {
                sendTwitterTweet(targetOption.getAsMember(), contentOption.getAsString(), commandEvent);
            } else {
                if (targetOption == null) Main.getInstance().getCommandManager().sendMessage("No User was given to use for the Tweet!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                if (contentOption == null) Main.getInstance().getCommandManager().sendMessage("No Tweet Content was given!" , 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }

        } else {
            if (commandEvent.getArguments().length >= 2) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    Main.getInstance().getCommandManager().sendMessage("No User given!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 1; i < commandEvent.getArguments().length; i++) {
                        stringBuilder.append(commandEvent.getArguments()[i]).append(" ");
                    }

                    sendTwitterTweet(commandEvent.getMessage().getMentionedMembers().get(0), stringBuilder.toString(), commandEvent);
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "twitter @User Yourtexthere", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("twitter", "Let the mentioned User Tweet something!")
                .addOptions(new OptionData(OptionType.USER, "target", "The User that should tweet something!").setRequired(true))
                .addOptions(new OptionData(OptionType.STRING, "content", "The Tweet Content!").setRequired(true));
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    public void sendTwitterTweet(Member member, String content, CommandEvent commandEvent) {
        try {


            String name = URLEncoder.encode(member.getUser().getName(), StandardCharsets.UTF_8);
            String text = URLEncoder.encode(content, StandardCharsets.UTF_8);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.dagpi.xyz/image/tweet/?url=" + member.getUser().getAvatarUrl() + "&username=" + name + "&text=" + text);
            request.setHeader("Authorization", Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken"));
            HttpResponse response = httpClient.execute(request);

            commandEvent.getTextChannel().sendFile(response.getEntity().getContent(), "twitter.png").queue();

            if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        } catch (Exception ex) {
            Main.getInstance().getCommandManager().sendMessage("Error while creating the Tweet!\nError: " + ex.getMessage().replaceAll(Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

}
