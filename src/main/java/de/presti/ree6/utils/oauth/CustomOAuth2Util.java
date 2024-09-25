package de.presti.ree6.utils.oauth;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.TwitchIntegration;

import java.util.Optional;

/**
 * Class used to convert a OAuth2Credential to a CustomOAuth2Credential.
 */
public class CustomOAuth2Util {

    /**
     * A bridge method to convert a CustomOAuth2Credential to an enriched CustomOAuth2Credential.
     * @param customOAuth2Credential The CustomOAuth2Credential to convert.
     * @return The converted CustomOAuth2Credential.
     */
    public static CustomOAuth2Credential convert(CustomOAuth2Credential customOAuth2Credential) {
        return convert(customOAuth2Credential.getDiscordId(), new OAuth2Credential(customOAuth2Credential.getIdentityProvider(),
                customOAuth2Credential.getAccessToken(), customOAuth2Credential.getRefreshToken(), customOAuth2Credential.getUserId(),
                customOAuth2Credential.getUserName(), customOAuth2Credential.getExpiresIn(), customOAuth2Credential.getScopes()));
    }

    /**
     * Converts a OAuth2Credential to a CustomOAuth2Credential.
     * @param discordId The Discord ID of the owner.
     * @param oAuth2Credential The OAuth2Credential to convert.
     * @return The converted CustomOAuth2Credential.
     */
    public static CustomOAuth2Credential convert(long discordId, OAuth2Credential oAuth2Credential) {
        // OAuth2
        OAuth2IdentityProvider oAuth2IdentityProvider = Main.getInstance().getNotifier().getCredentialManager().getIdentityProviderByName("twitch")
                .filter(idp -> idp.getProviderType().equalsIgnoreCase("oauth2") && idp instanceof OAuth2IdentityProvider)
                .map(idp -> (OAuth2IdentityProvider) idp)
                .orElseThrow(() -> new RuntimeException("Can't find a unique identity provider for the specified credential!"));

        Optional<OAuth2Credential> enrichedCredential = oAuth2IdentityProvider.getAdditionalCredentialInformation(oAuth2Credential);
        if (enrichedCredential.isPresent()) {
            oAuth2Credential = enrichedCredential.get();
        }

        return new CustomOAuth2Credential(discordId, oAuth2Credential);
    }

    /**
     * Converts a TwitchIntegration to a CustomOAuth2Credential.
     * @param twitchIntegration The TwitchIntegration to convert.
     * @return The converted CustomOAuth2Credential.
     */
    public static CustomOAuth2Credential convert(TwitchIntegration twitchIntegration) {
        return new CustomOAuth2Credential(twitchIntegration.getUserId(), "twitch",twitchIntegration.getToken(),
                twitchIntegration.getRefresh(), twitchIntegration.getChannelId(), twitchIntegration.getName(),
                twitchIntegration.getExpiresIn(), null);
    }
}
