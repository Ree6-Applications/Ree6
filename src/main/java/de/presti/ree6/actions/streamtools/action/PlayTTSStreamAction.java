package de.presti.ree6.actions.streamtools.action;

import de.presti.ree6.actions.ActionInfo;
import de.presti.ree6.actions.streamtools.IStreamAction;
import de.presti.ree6.actions.streamtools.StreamActionEvent;
import de.presti.ree6.main.Main;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * StreamAction used to play a text-to-speech messages.
 */
@NoArgsConstructor
@ActionInfo(name = "PlayTTS", command = "play-tts", description = "Plays a text-to-speech message.", introduced = "2.2.0")
public class PlayTTSStreamAction implements IStreamAction {

    /**
     * @see IStreamAction#runAction(StreamActionEvent)
     */
    @Override
    public boolean runAction(@NotNull StreamActionEvent event) {
        if (event.getArguments() == null || event.getArguments().length == 0) {
            return false;
        }

        boolean useTTSMonster = Arrays.stream(event.getArguments()).filter(Objects::nonNull).anyMatch(s -> s.startsWith("--ttsmonster:"));

        if (!Main.getInstance().getMusicWorker().isConnectedMember(event.getGuild().getSelfMember())) return false;

        String ttsMessage = String.join(" ", event.getArguments()).split("--ttsmonster:")[0];

        String baseUrl = /*useTTSMonster ? "NOT YET IMPLEMENTED" : */"https://api.streamelements.com/kappa/v2/speech?voice=Brian&text=";

        if (useTTSMonster) return true;

        Main.getInstance().getMusicWorker().loadAndPlay(event.getGuild(), null, null,
                baseUrl + URLEncoder.encode(ttsMessage, StandardCharsets.UTF_8),
                null, true, false);

        return true;
    }
}
