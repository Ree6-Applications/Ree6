package de.presti.ree6.streamtools;

import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.StreamAction;

import java.util.List;
import java.util.Map;

public class StreamActionContainerCreator {

    public static List<StreamActionContainer> getContainers(StreamAction.StreamListener listener) {
        return SQLSession.getSqlConnector().getSqlWorker()
                .getEntityList(new StreamAction(), "SELECT * FROM StreamActions WHERE listener = :listener", Map.of("listener", listener))
                .stream().map(StreamActionContainer::new).toList();
    }

}
