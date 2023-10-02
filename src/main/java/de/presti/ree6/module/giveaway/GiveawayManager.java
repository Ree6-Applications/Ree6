package de.presti.ree6.module.giveaway;

import de.presti.ree6.module.IManager;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Giveaway;

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
        replace(SQLSession.getSqlConnector().getSqlWorker()
                .getEntityList(new Giveaway(), "FROM Giveaway", null));
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

        Giveaway giveaway = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Giveaway(), "FROM Giveaway WHERE messageId = :id", Map.of("id", value));

        if (giveaway != null) {
            giveaways.add(giveaway);
            return giveaway;
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Giveaway> getList() {
        return giveaways;
    }
}
