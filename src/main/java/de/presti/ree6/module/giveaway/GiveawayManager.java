package de.presti.ree6.module.giveaway;

import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.module.IManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for the Giveaways.
 */
public class GiveawayManager implements IManager<InviteContainer> {

    /**
     * Constructor.
     */
    public GiveawayManager() {
        load();
    }

    /**
     * The List of Giveaways.
     */
    private final List<InviteContainer> giveaways = new ArrayList<>();

    /**
     * @inheritDoc
     */
    @Override
    public void load() {

    }

    /**
     * @inheritDoc
     */
    @Override
    public InviteContainer get(String value) {
        for (InviteContainer giveaway : giveaways) {
            if (giveaway.getCode().equals(value)) {
                return giveaway;
            }
        }

        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public InviteContainer get(long value) {
        return get(String.valueOf(value));
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<InviteContainer> getList() {
        return giveaways;
    }
}
