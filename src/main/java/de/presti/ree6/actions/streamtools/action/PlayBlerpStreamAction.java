package de.presti.ree6.actions.streamtools.action;

import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.RegExUtil;
import de.presti.ree6.utils.external.RequestUtility;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StreamAction used to play a blerp sound bound to a specific channel reward.
 */
@NoArgsConstructor
@ActionInfo(name = "PlayBlerp", command = "play-blerp", description = "Plays the blerp audio of a reward.", introduced = "2.4.0")
public class PlayBlerpStreamAction implements IStreamAction {

    /**
     * A {@link Pattern} to actually detect it.
     */
    Pattern blerpPattern = Pattern.compile(RegExUtil.BLERP_REGEX);

    /**
     * A {@link Pattern} to actually detect it.
     */
    Pattern blerpPagePattern = Pattern.compile(RegExUtil.BLERP_PAGE_REGEX);

    /**
     * @see IStreamAction#runAction(StreamActionEvent)
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (event.getEvent() == null) return false;

        if (!Main.getInstance().getMusicWorker().isConnectedMember(event.getGuild().getSelfMember())) return false;

        if (event.getEvent() instanceof RewardRedeemedEvent rewardRedeemedEvent) {
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
