package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.UserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.time.format.DateTimeFormatter;

/**
 * A command to show the user's information.
 */
@Command(name = "info", description = "command.description.info", category = Category.INFO)
public class Info implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.isSlashCommand()) {
            OptionMapping targetOption = commandEvent.getOption("target");

            if (targetOption != null && targetOption.getAsMember() != null) {
                sendInfo(targetOption.getAsMember(), commandEvent);
            } else {
                sendInfo(commandEvent.getMember(), commandEvent);
            }

        } else {
            if (commandEvent.getArguments().length == 1) {
                if (commandEvent.getMessage().getMentions().getMembers().isEmpty()) {
                    commandEvent.reply(commandEvent.getResource("message.default.noMention.user"), 5);
                    commandEvent.reply(commandEvent.getResource("message.default.usage","info @user"), 5);
                } else {
                    sendInfo(commandEvent.getMessage().getMentions().getMembers().get(0), commandEvent);
                }
            } else {
                sendInfo(commandEvent.getMember(), commandEvent);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("info", LanguageService.getDefault("command.description.info"))
                .addOptions(new OptionData(OptionType.USER, "target", "The User whose profile Information you want.").setRequired(false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }

    /**
     * Sends the Information about a User.
     * @param member The User to get the Information from.
     * @param commandEvent The CommandEvent.
     */
    public void sendInfo(Member member, CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        em.setTitle(member.getEffectiveName() + (UserUtil.isSupporter(member) ? " <a:duckswing:1070690323459735682>" : ""));
        em.setThumbnail(member.getEffectiveAvatarUrl());

        if (member.getUser().getDiscriminator().equals("0000")) {
            em.addField("**Username**", member.getUser().getName(), true);
        } else {
            em.addField("**UserTag**", member.getUser().getAsTag(), true);
        }

        em.addField("**Created Date**", member.getTimeCreated().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);
        em.addField("**Joined Date**", member.getTimeJoined().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), true);

        em.setFooter(commandEvent.getResource("label.footerMessage", commandEvent.getMember().getEffectiveName(), BotConfig.getAdvertisement()), commandEvent.getMember().getEffectiveAvatarUrl());

        commandEvent.reply(em.build());
    }
}
