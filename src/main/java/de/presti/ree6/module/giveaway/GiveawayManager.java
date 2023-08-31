package de.presti.ree6.module.giveaway;

import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.module.IManager;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class GiveawayManager implements IManager<InviteContainer> {

    private final List<InviteContainer> giveaways = new ArrayList<>();

    @Override
    public void load() {

    }

    @Override
    public InviteContainer get(String value) {
        for (InviteContainer giveaway : giveaways) {
            if (giveaway.getCode().equals(value)) {
                return giveaway;
            }
        }

        return null;
    }

    @Override
    public InviteContainer get(long value) {
        return get(String.valueOf(value));
    }

    @Override
    public List<InviteContainer> getList() {
        return giveaways;
    }
}
