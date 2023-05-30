package de.presti.ree6.actions.streamtools.action;

import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.external.RequestUtility;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StreamAction used to play a blerp sound that is bound to a specific channel reward.
 */
@NoArgsConstructor
@ActionInfo(name = "PlayBlerp", command = "play-blerp", description = "Plays the blerp audio of a reward.", introduced = "2.4.0")
public class PlayBlerpStreamAction implements IStreamAction {

    /**
     * RegEx to detect a Blerp link in the redemption itself.
     */
    String blerpRegEx = "https:\\/\\/blerp\\.com\\/soundbites\\/[a-zA-Z0-9]+";

    /**
     * RegEx to take the CDN Url of that sound out of the HTML content.
     */
    String blerpPageRegEx = "https:\\/\\/cdn\\.blerp\\.com\\/normalized\\/[a-zA-Z0-9]+";

    /**
     * A {@link Pattern} to actually detect it.
     */
    Pattern blerpPattern = Pattern.compile(blerpRegEx);

    /**
     * A {@link Pattern} to actually detect it.
     */
    Pattern blerpPagePattern = Pattern.compile(blerpPageRegEx);

    /**
     * @inheritDoc
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (event.getTwitchEvent() == null) return false;

        if (!Main.getInstance().getMusicWorker().isConnectedMember(event.getGuild().getSelfMember())) return false;

        if (event.getTwitchEvent() instanceof RewardRedeemedEvent rewardRedeemedEvent) {
            String prompt = rewardRedeemedEvent.getRedemption().getReward().getPrompt().toLowerCase();
            Matcher matcher = blerpPattern.matcher(prompt);
            if (matcher.find()) {
                String blerpUrl = matcher.group(1);

                String blerpPageResponse = RequestUtility.requestString(RequestUtility.Request.builder().url(blerpUrl).GET().build());

                Matcher pageMatcher = blerpPagePattern.matcher(blerpPageResponse);
                if (matcher.find()) {
                    Main.getInstance().getMusicWorker().loadAndPlay(event.getGuild(), null, null,
                            pageMatcher.group(1),null, true, false);
                    return true;
                }
            }
        }

        return false;
    }
}
