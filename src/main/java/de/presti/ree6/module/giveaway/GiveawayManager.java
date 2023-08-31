package de.presti.ree6.module.giveaway;

import de.presti.ree6.module.IManager;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Giveaway;

import java.util.ArrayList;
import java.util.List;

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
