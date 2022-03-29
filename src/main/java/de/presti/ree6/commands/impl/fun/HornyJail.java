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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

@Command(name = "hornyjail", description = "Put someone into the Horny-Jail", category = Category.FUN)
public class HornyJail implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getSlashCommandInteractionEvent().getOption("target");

            if (targetOption != null) {
                sendHornyJail(targetOption.getAsMember(), commandEvent);
            } else {
                Main.getInstance().getCommandManager().sendMessage("No User was given to put into the Hornyjail!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentionedMembers().isEmpty()) {
                    Main.getInstance().getCommandManager().sendMessage("No User given!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    sendHornyJail(commandEvent.getMessage().getMentionedMembers().get(0), commandEvent);
                }
            } else {
                Main.getInstance().getCommandManager().sendMessage("Use " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "hornyjail @User", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("hornyjail", "Put someone in the Hornyjail!").addOptions(new OptionData(OptionType.USER, "target", "The User that should be put into the Hornyjail!").setRequired(true));
    }

    @Override
    public String[] getAlias() {
        return new String[]{"horny", "jail"};
    }

    public void sendHornyJail(Member member, CommandEvent commandEvent) {
        try {

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.dagpi.xyz/image/jail/?url=" + member.getUser().getAvatarUrl());
            request.setHeader("Authorization", Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken"));
            HttpResponse response = httpClient.execute(request);

            Main.getInstance().getCommandManager().sendMessage(member.getAsMention() + " is now in the Hornyjail!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            commandEvent.getTextChannel().sendFile(response.getEntity().getContent(), "hornyjail.png").queue();
            if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        } catch (Exception ex) {
            Main.getInstance().getCommandManager().sendMessage("Error while putting someone in the Hornyjail!\nError: " + ex.getMessage().replaceAll(Main.getInstance().getConfig().getConfiguration().getString("dagpi.apitoken"), "Ree6TopSecretAPIToken"), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}