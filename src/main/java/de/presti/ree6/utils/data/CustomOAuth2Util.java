package de.presti.ree6.utils.data;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import de.presti.ree6.main.Main;
import java.util.Optional;

/**
 * Class used to convert a OAuth2Credential to a CustomOAuth2Credential.
 */
public class CustomOAuth2Util {

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
}
