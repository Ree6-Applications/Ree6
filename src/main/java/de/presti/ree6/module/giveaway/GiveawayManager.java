package de.presti.ree6.module.giveaway;

import de.presti.ree6.module.IManager;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Giveaway;
import de.presti.ree6.utils.others.RandomUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager for the Giveaways.
 */
public class GiveawayManager implements IManager<Giveaway> {

    /**
     * Constructor.
     */
    public GiveawayManager() {
        load();
    }

    /**
     * The List of Giveaways.
     */
    private final List<Giveaway> giveaways = new ArrayList<>();

    /**
     * @inheritDoc
     */
    @Override
    public void load() {
        SQLSession.getSqlConnector().getSqlWorker()
                .getEntityList(new Giveaway(), "FROM Giveaway", null)
                .subscribe(this::replace);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Giveaway get(String value) {
        return get(Long.parseLong(value));
    }

    /**
     * @inheritDoc
     */
    @Override
    public Giveaway get(long value) {
        for (Giveaway giveaway : giveaways) {
            if (giveaway.getMessageId() == value) {
                return giveaway;
            }
        }

        return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Giveaway(), "FROM Giveaway WHERE messageId = :id", Map.of("id", value)).mapNotNull(giveaway -> {
            if (giveaway.isPresent()) {
                giveaways.add(giveaway.get());
                return giveaway.get();
            }
            return null;
        }).block();
    }

    @Override
    public void remove(Giveaway object) {
        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(object).block();
        IManager.super.remove(object);
    }

    public String endGiveaway(Giveaway giveaway, MessageEditBuilder messageEditBuilder, List<User> users) {
        return endGiveaway(giveaway, messageEditBuilder, users, giveaway.getWinners());
    }

    public String endGiveaway(Giveaway giveaway, MessageEditBuilder messageEditBuilder, List<User> users, long winners) {
        StringBuilder stringBuilder = new StringBuilder();

        users.removeIf(User::isBot);

        for (int i = 0; i < Math.min(giveaway.getWinners(), Math.min(winners, users.size())); i++) {
            stringBuilder.append(users.get(RandomUtils.nextInt(0, users.size())).getAsMention()).append(", ");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder(messageEditBuilder.getEmbeds().get(0));

        String winnerText = stringBuilder.substring(0, stringBuilder.length() - 2);

        embedBuilder.setDescription("Ended: <t:" + (System.currentTimeMillis() / 1000) + ":R>\n" +
                "Winners: " + winnerText + "\n");

        messageEditBuilder.setEmbeds(embedBuilder.build());
        return winnerText;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Giveaway> getList() {
        return giveaways;
    }
}
