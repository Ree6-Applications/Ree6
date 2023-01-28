package de.presti.ree6.utils.data;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.TwitchIntegration;

import java.util.*;

public class DatabaseStorageBackend implements IStorageBackend {

    @Override
    public List<Credential> loadCredentials() {
        List<TwitchIntegration> twitchIntegrations =
                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(),
                        "SELECT * FROM TwitchIntegration", null);

        List<Credential> credentials = new ArrayList<>();

        twitchIntegrations.forEach(twitchIntegration -> credentials.add(new OAuth2Credential("twitch", twitchIntegration.getToken(),
                twitchIntegration.getRefresh(), twitchIntegration.getChannelId(), twitchIntegration.getName(), twitchIntegration.getExpiresIn(), Collections.emptyList())));

        return credentials;
    }

    @Override
    public void saveCredentials(List<Credential> list) {
        list.forEach(credential -> {
            if (credential instanceof OAuth2Credential oAuth2Credential) {
                TwitchIntegration twitchIntegration =
                        SQLSession.getSqlConnector().getSqlWorker().getEntity(new TwitchIntegration(),
                                "SELECT * FROM TwitchIntegration WHERE channelId = :userid", Map.of("userid",oAuth2Credential.getUserId()));

                if (twitchIntegration == null) {
                    twitchIntegration = new TwitchIntegration();
                    twitchIntegration.setChannelId(oAuth2Credential.getUserId());
                }

                twitchIntegration.setToken(oAuth2Credential.getAccessToken());
                twitchIntegration.setRefresh(oAuth2Credential.getRefreshToken());
                twitchIntegration.setName(oAuth2Credential.getUserName());
                twitchIntegration.setExpiresIn(oAuth2Credential.getExpiresIn());
                SQLSession.getSqlConnector().getSqlWorker().updateEntity(twitchIntegration);
            }
        });
    }

    @Override
    public Optional<Credential> getCredentialByUserId(String userId) {
        Optional<TwitchIntegration> twitchIntegration = Optional.ofNullable(SQLSession.getSqlConnector().getSqlWorker().getEntity(new TwitchIntegration(),
                "SELECT * FROM TwitchIntegration WHERE channelId = :userid", Map.of("userid", userId)));

        if (twitchIntegration.isPresent()) {
            TwitchIntegration twitchIntegration1 = twitchIntegration.get();
            OAuth2Credential oAuth2Credential
                    = new OAuth2Credential("twitch", twitchIntegration1.getToken(),
                    twitchIntegration1.getRefresh(), twitchIntegration1.getChannelId(), twitchIntegration1.getName(), twitchIntegration1.getExpiresIn(), Collections.emptyList());

            return Optional.of(oAuth2Credential);
        } else {
            return Optional.empty();
        }
    }
}
