package de.presti.ree6.commands.impl.community;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.utils.data.RegExUtil;
import de.presti.ree6.utils.others.RandomUtils;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Command to manage Giveaways.
 */
@Command(name = "giveaway", description = "command.description.giveaway", category = Category.COMMUNITY)
public class Giveaway implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        if (!commandEvent.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            commandEvent.reply(commandEvent.getResource("message.default.needPermission", Permission.MANAGE_WEBHOOKS.getName()));
            return;
        }

        switch (commandEvent.getSubcommand()) {
            case "create" -> {
                long winners = commandEvent.getOption("winners").getAsLong();
                String prize = commandEvent.getOption("prize").getAsString();
                String duration = commandEvent.getOption("duration").getAsString();

                long days = 0, hours = 0, minutes = 0;

                Matcher matcher = Pattern.compile(RegExUtil.TIME_INPUT_REGEX).matcher(duration);

                Set<Character> letters = new HashSet<>();

                while (matcher.find()) {
                    String match = matcher.group();
                    char letter = match.charAt(match.length() - 1);
                    if (!letters.contains(letter)) {
                        long value = Long.parseLong(match.substring(0, match.length() - 1));

                        if (letter == 'd') {
                            days = value;
                        } else if (letter == 'h') {
                            hours = value;
                        } else {
                            minutes = value;
                        }

                        letters.add(letter);
                    }
                }

                Instant endInstant = Instant.now();
                endInstant = endInstant.plusSeconds(minutes * 60);
                endInstant = endInstant.plusSeconds(hours * 60 * 60);
                endInstant = endInstant.plusSeconds(days * 24 * 60 * 60);

                Timestamp endTime = Timestamp.from(endInstant);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(prize);

                embedBuilder.setDescription("Ending: <t:" + endInstant.getEpochSecond() + ":R>\n" +
                        "Hosted by: " + commandEvent.getMember().getAsMention() + "\n");

                commandEvent.getChannel().sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                    message.addReaction(Emoji.fromUnicode("U+1F389")).queue();
                    de.presti.ree6.sql.entities.Giveaway giveaway =
                            new de.presti.ree6.sql.entities.Giveaway(message.getIdLong(), commandEvent.getMember().getIdLong(),
                                    commandEvent.getGuild().getIdLong(), commandEvent.getChannel().getIdLong(), prize, winners, endTime);

                    giveaway = SQLSession.getSqlConnector().getSqlWorker().updateEntity(giveaway);
                    Main.getInstance().getGiveawayManager().add(giveaway);
                });

                commandEvent.reply(commandEvent.getResource("message.giveaway.created"));
            }

            case "end" -> {
                String id = commandEvent.getOption("id").getAsString();

                if (!id.matches(RegExUtil.NUMBER_REGEX)) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                long idLong;

                try {
                    idLong = Long.parseLong(id);
                } catch (Exception ignore) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                de.presti.ree6.sql.entities.Giveaway giveaway = Main.getInstance().getGiveawayManager().get(idLong);

                if (giveaway == null || giveaway.getGuildId() != commandEvent.getGuild().getIdLong()) {
                    commandEvent.reply(commandEvent.getResource("message.giveaway.notFound"));
                    return;
                }

                if (giveaway.getEnding().before(Timestamp.from(Instant.now()))) {
                    commandEvent.reply(commandEvent.getResource("message.giveaway.ended"));
                    return;
                }

                GuildMessageChannelUnion channel = commandEvent.getGuild().getChannelById(GuildMessageChannelUnion.class, giveaway.getChannelId());

                if (channel == null) {
                    Main.getInstance().getGiveawayManager().remove(giveaway);
                    return;
                }

                channel.retrieveMessageById(giveaway.getMessageId()).onErrorMap(throwable -> {
                    Main.getInstance().getGiveawayManager().remove(giveaway);
                    return null;
                }).queue(message -> {
                    if (message == null)
                        return;

                    MessageReaction reaction = message.getReaction(Emoji.fromUnicode("U+1F389"));

                    if (reaction == null) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                        return;
                    }

                    if (!reaction.hasCount()) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                        return;
                    }

                    if (reaction.getCount() < 2) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.less"));
                        return;
                    }

                    reaction.retrieveUsers().mapToResult().onErrorMap(throwable -> {
                        Main.getInstance().getGiveawayManager().remove(giveaway);
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.error"));
                        return null;
                    }).queue(users -> {
                        if (users == null) {
                            return;
                        }

                        users.onSuccess(userList -> {
                            if (userList.isEmpty()) {
                                commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                                return;
                            }

                            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage(message);
                            commandEvent.reply(commandEvent.getResource("message.giveaway.finish",
                                    Main.getInstance().getGiveawayManager().endGiveaway(giveaway, messageEditBuilder, userList)));
                            message.editMessage(messageEditBuilder.build()).queue();
                        }).onFailure(Sentry::captureException);
                    });
                });
            }

            case "reroll" -> {
                String id = commandEvent.getOption("id").getAsString();

                if (!id.matches(RegExUtil.NUMBER_REGEX)) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                long idLong;

                try {
                    idLong = Long.parseLong(id);
                } catch (Exception ignore) {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"));
                    return;
                }

                long winners = commandEvent.getOption("winners").getAsLong();

                de.presti.ree6.sql.entities.Giveaway giveaway = Main.getInstance().getGiveawayManager().get(idLong);

                if (giveaway == null || giveaway.getGuildId() != commandEvent.getGuild().getIdLong()) {
                    commandEvent.reply(commandEvent.getResource("message.giveaway.notFound"));
                    return;
                }

                if (giveaway.getEnding().after(Timestamp.from(Instant.now()))) {
                    commandEvent.reply(commandEvent.getResource("message.giveaway.notEnded"));
                    return;
                }

                GuildMessageChannelUnion channel = commandEvent.getGuild().getChannelById(GuildMessageChannelUnion.class, giveaway.getChannelId());

                if (channel == null) {
                    Main.getInstance().getGiveawayManager().remove(giveaway);
                    return;
                }

                channel.retrieveMessageById(giveaway.getMessageId()).onErrorMap(throwable -> {
                    Main.getInstance().getGiveawayManager().remove(giveaway);
                    return null;
                }).queue(message -> {
                    if (message == null)
                        return;

                    MessageReaction reaction = message.getReaction(Emoji.fromUnicode("U+1F389"));

                    if (reaction == null) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                        return;
                    }

                    if (!reaction.hasCount()) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                        return;
                    }

                    if (reaction.getCount() < 2) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.less"));
                        return;
                    }

                    if (reaction.getCount() < winners) {
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.less"));
                        return;
                    }

                    reaction.retrieveUsers().mapToResult().onErrorMap(throwable -> {
                        Main.getInstance().getGiveawayManager().remove(giveaway);
                        commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.error"));
                        return null;
                    }).queue(users -> {
                        if (users == null) {
                            return;
                        }

                        users.onSuccess(userList -> {
                            if (userList.isEmpty()) {
                                commandEvent.reply(commandEvent.getResource("message.giveaway.reaction.none"));
                                return;
                            }

                            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage(message);
                            commandEvent.reply(commandEvent.getResource("message.giveaway.reroll",
                                    Main.getInstance().getGiveawayManager().endGiveaway(giveaway, messageEditBuilder, userList, winners)));
                            message.editMessage(messageEditBuilder.build()).queue();
                        }).onFailure(Sentry::captureException);
                    });
                });
            }

            default -> {
                StringBuilder stringBuilder = new StringBuilder("```");
                for (de.presti.ree6.sql.entities.Giveaway giveaway : Main.getInstance().getGiveawayManager().getList()) {
                    stringBuilder.append(commandEvent.getResource("message.giveaway.list.entry", giveaway.getMessageId(), giveaway.getChannelId(), giveaway.getWinners(), giveaway.getPrize(), giveaway.getEnding()));
                }
                stringBuilder.append("```");
                commandEvent.reply(commandEvent.getResource("message.giveaway.list.default") + " " + (stringBuilder.length() == 6 ? "None" : stringBuilder));
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("giveaway", LanguageService.getDefault("command.description.giveaway"))
                .addSubcommands(new SubcommandData("create", "Create a Giveaway.")
                                .addOption(OptionType.STRING, "prize", "The Prize of the Giveaway.", true)
                                .addOption(OptionType.INTEGER, "winners", "The amount of winners.", true)
                                .addOption(OptionType.STRING, "duration", "The duration of the Giveaway.", true),
                        new SubcommandData("end", "End a Giveaway.")
                                .addOption(OptionType.STRING, "id", "The Message ID of the Giveaway.", true),
                        new SubcommandData("reroll", "Reroll a Giveaway.")
                                .addOption(OptionType.STRING, "id", "The Message ID of the Giveaway.", true)
                                .addOption(OptionType.INTEGER, "winners", "The amount of winners.", true),
                        new SubcommandData("list", "List all Giveaways."));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
