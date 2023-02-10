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
import de.presti.ree6.utils.data.Data;
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
 * A command used to search for mangas!
 */
@Command(name = "manga", description = "command.description.manga", category = Category.FUN)
public class Manga implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        Message message = commandEvent.isSlashCommand() ?
                commandEvent.getInteractionHook().sendMessage(commandEvent.getResource("message.manga.searching")).complete() :
                commandEvent.getChannel().sendMessage(commandEvent.getResource("message.manga.searching")).complete();

        String[] args = commandEvent.getArguments();

        if (commandEvent.isSlashCommand()) {
            OptionMapping searchQueryMapping = commandEvent.getSlashCommandInteractionEvent().getOption("search");
            if (searchQueryMapping != null)
                args = searchQueryMapping.getAsString().split(" ");
        }

        StringBuilder builder = new StringBuilder();

        for (final String string : args)
            builder.append(string).append(' ');

        if (builder.toString().endsWith(" "))
            builder = new StringBuilder(builder.substring(0, builder.length() - 1));

        if (args.length > 0) {
            sendManga(commandEvent, message, builder.toString());
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
    public void sendManga(CommandEvent commandEvent, Message message, String query) {
        RequestUtility.Request request = RequestUtility.Request.builder()
                .url("https://kitsu.io/api/edge/manga?filter[text]=" + URLEncoder.encode(query, StandardCharsets.UTF_8))
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
                    attributes.get("canonicalTitle").getAsString() : commandEvent.getResource("message.manga.error");

            String thumbnailUrl = attributes.has("posterImage") &&
                    attributes.get("posterImage").isJsonObject() &&
                    attributes.getAsJsonObject("posterImage").has("large") ?
                    attributes.getAsJsonObject("posterImage").get("large").getAsString() : null;

            String description = attributes.has("synopsis") ?
                    attributes.get("synopsis").getAsString() : "?";

            String status = attributes.has("status") ?
                    attributes.get("status").getAsString() : "?";

            String type = attributes.has("mangaType") ?
                    attributes.get("mangaType").getAsString() : "?";

            String genres = attributes.has("genres") &&
                    attributes.get("genres").isJsonArray() ?
                    attributes.getAsJsonArray("genres").toString() : tryResolvingGenres(data);

            String startDate = attributes.has("startDate") ?
                    attributes.get("startDate").getAsString() : "?";

            String endDate = attributes.has("endDate") && attributes.get("endDate").isJsonPrimitive() ?
                    attributes.get("endDate").getAsString() : "?";

            String chapters = attributes.has("chapterCount") ?
                    attributes.get("chapterCount").getAsString() : "?";

            String volumes = attributes.has("volumeCount") ?
                    attributes.get("volumeCount").getAsString() : "?";

            String rating = attributes.has("averageRating") ?
                    attributes.get("averageRating").getAsString() : "?";

            String rank = attributes.has("ratingRank") ?
                    attributes.get("ratingRank").getAsString() : "?";

            EmbedBuilder em = new EmbedBuilder();

            em.setTitle(name, url);
            em.setThumbnail(thumbnailUrl);
            em.setDescription(description);
            em.addField(":hourglass_flowing_sand: **" + commandEvent.getResource("label.status") + "**", status, true);
            em.addField(":dividers: **" + commandEvent.getResource("label.type") + "**", type, true);
            em.addField(":arrow_right: **" + commandEvent.getResource("label.genres") + "**", genres, false);
            em.addField(":calendar: **" + commandEvent.getResource("label.published") + "**", "from **" + startDate + "** to **" + endDate + "**", false);
            em.addField(":newspaper: **" + commandEvent.getResource("label.chapters") + "**", chapters, true);
            em.addField(":books: **" + commandEvent.getResource("label.volumes") + "**", volumes + "", true);
            em.addField(":star: **" + commandEvent.getResource("label.averageRating") + "**", " **" + rating + "/100**", true);
            em.addField(":trophy: **" + commandEvent.getResource("label.rank") + "**", "**TOP " + rank + "**", true);
            em.setFooter(commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

            if (commandEvent.isSlashCommand()) {
                message.editMessage(commandEvent.getResource("message.manga.found")).queue();
                Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getChannel(), null);
            } else {
                message.editMessageEmbeds(em.build()).queue(message1 -> message1.editMessage(commandEvent.getResource("message.manga.found")).queue());
            }
        } else {
            message.editMessage(commandEvent.getResource("message.manga.error")).queue();
        }
    }

    /**
     * Try to resolve the genres.
     * @param data the data.
     * @return the genres.
     */
    public String tryResolvingGenres(JsonObject data) {
        if (data.has("relationships") &&
                data.get("relationships").isJsonObject() &&
                data.getAsJsonObject("relationships").has("genres")) {
            JsonObject genres = data.getAsJsonObject("relationships").getAsJsonObject("genres");

            if (genres.has("links") &&
                    genres.get("links").isJsonObject() &&
                    genres.getAsJsonObject("links").has("related")) {
                String url = genres.getAsJsonObject("links").get("related").getAsString();

                RequestUtility.Request request = RequestUtility.Request.builder()
                        .url(url)
                        .build();

                JsonElement jsonElement = RequestUtility.requestJson(request);

                if (jsonElement != null &&
                        jsonElement.isJsonObject() &&
                        jsonElement.getAsJsonObject().has("data") &&
                        jsonElement.getAsJsonObject().get("data").isJsonArray()) {
                    StringBuilder builder = new StringBuilder();
                    for (JsonElement jsonElement1 : jsonElement.getAsJsonObject().get("data").getAsJsonArray()) {
                        if (jsonElement1.isJsonObject()) {
                            JsonObject jsonObject = jsonElement1.getAsJsonObject();
                            if (jsonObject.has("attributes") && jsonObject.get("attributes").isJsonObject()) {
                                JsonObject attributes = jsonObject.getAsJsonObject("attributes");
                                if (attributes.has("name")) {
                                    builder.append(attributes.get("name").getAsString()).append(", ");
                                }
                            }
                        }
                    }
                    if (builder.length() > 0)
                        return builder.substring(0, builder.length() - 2);
                }
            }
        }
        return "?";
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("manga", LanguageService.getDefault("command.description.manga"))
                .addOption(OptionType.STRING, "search", "The search query to search for.", true);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
