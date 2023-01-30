package de.presti.ree6.utils.data;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Custom OAuth2Credential to contain the discord user id.
 */
public class CustomOAuth2Credential extends OAuth2Credential {

    /**
     * The Discord ID of the owner.
     */
    @Setter
    @Getter
    long discordId;

    /**
     * Constructor.
     * @param oAuth2Credential the original credential.
     */
    public CustomOAuth2Credential(OAuth2Credential oAuth2Credential) {
        super(oAuth2Credential.getIdentityProvider(), oAuth2Credential.getAccessToken(), oAuth2Credential.getRefreshToken(),
                oAuth2Credential.getUserId(), oAuth2Credential.getUserName(), oAuth2Credential.getExpiresIn(),
                oAuth2Credential.getScopes(),oAuth2Credential.getContext());
    }

    /**
     * Constructor.
     *
     * @param discordId        the Discord ID of the Owner.
     * @param identityProvider the Identity Provider of the Token.
     * @param accessToken      the access token itself.
     */
    public CustomOAuth2Credential(long discordId, String identityProvider, String accessToken) {
        super(identityProvider, accessToken);
        this.discordId = discordId;
    }

    /**
     * Constructor.
     *
     * @param discordId        the Discord ID of the Owner.
     * @param identityProvider the Identity Provider of the Token.
     * @param accessToken      the access token itself.
     * @param context          the response context.
     */
    public CustomOAuth2Credential(long discordId, String identityProvider, String accessToken, @NotNull Map<String, Object> context) {
        super(identityProvider, accessToken, context);
        this.discordId = discordId;
    }

    /**
     * Constructor.
     *
     * @param discordId        the Discord ID of the Owner.
     * @param identityProvider the Identity Provider of the Token.
     * @param accessToken      the access token itself.
     * @param refreshToken     the refresh token itself.
     * @param userId           User Id
     * @param userName         User Name
     * @param expiresIn        Expires in x seconds
     * @param scopes           Scopes
     */
    public CustomOAuth2Credential(long discordId, String identityProvider, String accessToken, String refreshToken, String userId, String userName, Integer expiresIn, List<String> scopes) {
        super(identityProvider, accessToken, refreshToken, userId, userName, expiresIn, scopes);
        this.discordId = discordId;
    }

    /**
     * Constructor.
     *
     * @param discordId        the Discord ID of the Owner.
     * @param identityProvider the Identity Provider of the Token.
     * @param accessToken      the access token itself.
     * @param refreshToken     the refresh token itself.
     * @param userId           User Id
     * @param userName         User Name
     * @param expiresIn        Expires in x seconds
     * @param scopes           Scopes
     * @param context          the response context.
     */
    public CustomOAuth2Credential(long discordId, String identityProvider, String accessToken, String refreshToken, String userId, String userName, Integer expiresIn, List<String> scopes, Map<String, Object> context) {
        super(identityProvider, accessToken, refreshToken, userId, userName, expiresIn, scopes, context);
        this.discordId = discordId;
    }
}
