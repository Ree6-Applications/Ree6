package de.presti.ree6.module.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SonicIdentifier {
    private String identifier;

    public long getIdentifierAsLong() {
        return Long.parseLong(this.identifier);
    }
}
