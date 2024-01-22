package de.presti.ree6.commands.impl.fun;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A command used to search for animes!
 */
@Command(name = "anime", description = "command.description.anime", category = Category.FUN)
public class Anime implements ICommand {

    /**
     * @see ICommand#onPerform(CommandEvent)
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.anime.searching")).complete() :
                commandEvent.getChannel().sendMessage(commandEvent.getResource("message.anime.searching")).complete();

        String[] args = commandEvent.getArguments();

        if (commandEvent.isSlashCommand()) {
            OptionMapping searchQueryMapping = commandEvent.getOption("search");
            if (searchQueryMapping != null)
                args = searchQueryMapping.getAsString().split(" ");
        }

        StringBuilder builder = new StringBuilder();

        for (final String string : args)
            builder.append(string).append(' ');

        if (builder.toString().endsWith(" "))
            builder = new StringBuilder(builder.substring(0, builder.length() - 1));

        if (args.length > 0) {
            sendAnime(commandEvent, message, builder.toString());
        } else {
            message.editMessage(commandEvent.getResource("message.default.invalidQuery")).queue();
        }
    }

    /**
     * Send the anime to the channel.
     * @param commandEvent the CommandEvent.
     * @param message the Message.
     * @param query the query.
     */
    public void sendAnime(CommandEvent commandEvent, Message message, String query) {
        RequestUtility.Request request = RequestUtility.Request.builder()
                .url("https://kitsu.io/api/edge/anime?filter[text]=" + URLEncoder.encode(query, StandardCharsets.UTF_8))
                .build();
        JsonElement jsonElement = RequestUtility.requestJson(request);

        if (jsonElement != null &&
                jsonElement.isJsonObject() &&
                jsonElement.getAsJsonObject().has("data") &&
                jsonElement.getAsJsonObject().get("data").isJsonArray()) {
            JsonArray dataArray = jsonElement.getAsJsonObject().getAsJsonArray("data");

            JsonObject data = !dataArray.isEmpty() && dataArray.get(0).isJsonObject() ?
                    dataArray.get(0).getAsJsonObject() : new JsonObject();

            JsonObject attributes = data.has("attributes") && data.get("attributes").isJsonObject()
                    ? data.getAsJsonObject("attributes") : new JsonObject();

            String url = data.has("links") &&
                    data.get("links").isJsonObject() && data.getAsJsonObject("links").has("self") ?
                    data.getAsJsonObject("links").get("self").getAsString() : null;

            String name = attributes.has("canonicalTitle") ?
                    attributes.get("canonicalTitle").getAsString() : commandEvent.getResource("message.anime.error");

            String thumbnailUrl = attributes.has("posterImage") &&
                    attributes.get("posterImage").isJsonObject() &&
                    attributes.getAsJsonObject("posterImage").has("large") ?
                    attributes.getAsJsonObject("posterImage").get("large").getAsString() : null;

            String description = attributes.has("synopsis") && attributes.get("synopsis").isJsonPrimitive() ?
                    attributes.get("synopsis").getAsString() : "?";

            String status = attributes.has("status") && attributes.get("status").isJsonPrimitive() ?
                    attributes.get("status").getAsString() : "?";

            String type = attributes.has("showType") && attributes.get("showType").isJsonPrimitive() ?
                    attributes.get("showType").getAsString() : "?";

            String genres = attributes.has("genres") &&
                    attributes.get("genres").isJsonArray() ?
                    attributes.getAsJsonArray("genres").toString() : "?";

            String startDate = attributes.has("startDate") && attributes.get("startDate").isJsonPrimitive() ?
                    attributes.get("startDate").getAsString() : "?";

            String endDate = attributes.has("endDate") && attributes.get("endDate").isJsonPrimitive() ?
                    attributes.get("endDate").getAsString() : "?";

            String episodes = attributes.has("episodeCount") && attributes.get("episodeCount").isJsonPrimitive() ?
                    attributes.get("episodeCount").getAsString() : "?";

            String duration = attributes.has("totalLength") && attributes.get("totalLength").isJsonPrimitive() ?
                    attributes.get("totalLength").getAsInt() + " minutes" : "?";

            String rating = attributes.has("averageRating") && attributes.get("averageRating").isJsonPrimitive() ?
                    attributes.get("averageRating").getAsString() : "?";

            String rank = attributes.has("ratingRank") && attributes.get("ratingRank").isJsonPrimitive() ?
                    attributes.get("ratingRank").getAsString() : "?";

            EmbedBuilder em = new EmbedBuilder();

            em.setTitle(name, url);
            em.setThumbnail(thumbnailUrl);
            em.setDescription(description);
            em.addField(":hourglass_flowing_sand: **" + commandEvent.getResource("label.status") + "**", status, true);
            em.addField(":dividers: **" + commandEvent.getResource("label.typ") + "**", type, true);
            em.addField(":arrow_right: **" + commandEvent.getResource("label.genres") + "**", genres, false);
            em.addField(":calendar: **" + commandEvent.getResource("label.aired") + "**", "from **" + startDate + "** to **" + endDate + "**", false);
            em.addField(":minidisc: **" + commandEvent.getResource("label.episodes") + "**", episodes, true);
            em.addField(":stopwatch: **" + commandEvent.getResource("label.duration") + "**", duration, true);
            em.addField(":star: **" + commandEvent.getResource("label.averageRating") + "**", " **" + rating + "/100**", true);
            em.addField(":trophy: **" + commandEvent.getResource("label.rank") + "**", "**TOP " + rank + "**", true);
            em.setFooter(commandEvent.getMember().getEffectiveName() + " - " + BotConfig.getAdvertisement(), commandEvent.getMember().getEffectiveAvatarUrl());

            if (commandEvent.isSlashCommand()) {
                message.editMessage(commandEvent.getResource("message.anime.found")).queue();
                Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getChannel(), null);
            } else {
                message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage(commandEvent.getResource("message.anime.found")).queue());
            }
        } else {
            message.editMessage(commandEvent.getResource("message.anime.error")).queue();
        }
    }

    /**
     * @see ICommand#getCommandData()
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("anime", LanguageService.getDefault("command.description.anime"))
                .addOption(OptionType.STRING, "search", "The search query to search for.", true);
    }

    /**
     * @see ICommand#getAlias()
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
