package de.presti.ree6.commands.impl.nsfw;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.RandomUtils;
import io.sentry.Sentry;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A command to show NSFW-Image from r/hentai.
 */
@Command(name = "nsfw", description = "command.description.nsfw", category = Category.NSFW)
public class NSFW implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (commandEvent.getChannel().getType() == ChannelType.TEXT && commandEvent.getChannel().asTextChannel().isNSFW()) {
            sendImage(commandEvent);
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.onlyNSFW"), 5);
        }
    }

    /**
     * Method called to send the Image.
     *
     * @param commandEvent the CommandEvent.
     */
    public void sendImage(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete() :
                commandEvent.getChannel().sendMessage(commandEvent.getResource("message.nsfw.searching")).complete();

        List<String> images = new ArrayList<>();

        try {
            List<RedditPost> request = Main.getInstance().getNotifier().getRedditClient().getSubredditPosts("hentai", Sorting.HOT).limit(50).submit();

            request.forEach(x -> {
                String fileUrl = x.getUrl().toLowerCase();

                if (x.getMedia() == null &&
                        !fileUrl.startsWith("https://www.reddit.com/gallery/") &&
                        !fileUrl.startsWith("https://redgifs.com/")) {

                    if (fileUrl.endsWith(".jpg") ||
                            fileUrl.endsWith(".png") ||
                            fileUrl.endsWith(".jpeg") ||
                            fileUrl.endsWith(".gif") ||
                            fileUrl.endsWith(".webp")) {
                        images.add(x.getUrl());
                    }
                }
            });

            if (!images.isEmpty()) {
                String randomUrl = images.get(RandomUtils.secureRandom.nextInt(images.size() - 1));
                EmbedBuilder em = new EmbedBuilder();

                em.setImage(randomUrl);
                em.setFooter(commandEvent.getMember().getEffectiveName() + " - " + BotConfig.getAdvertisement(), commandEvent.getMember().getEffectiveAvatarUrl());

                if (commandEvent.isSlashCommand()) {
                    message.editMessage(commandEvent.getResource("message.default.checkBelow")).queue();
                    commandEvent.reply(em.build());
                } else {
                    message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage(commandEvent.getResource("message.default.checkBelow")).queue());
                }
            } else {
                message.editMessage(commandEvent.getResource("message.default.retrievalError")).delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
            }
        } catch (Exception exception) {
            log.error("Failed to load NSFW Images from Reddit!", exception);
            Sentry.captureException(exception);
            message.editMessage(commandEvent.getResource("message.default.retrievalError")).delay(Duration.ofSeconds(5)).flatMap(Message::delete).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[]{"givensfw", "hentai"};
    }
}
