package de.presti.ree6.streamtools.action.impl;

import de.presti.ree6.streamtools.action.ActionInfo;
import de.presti.ree6.streamtools.action.IAction;
import de.presti.ree6.utils.external.RequestUtility;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ActionInfo(name = "PlayTTS", command = "play-tts", description = "Plays a text-to-speech message.", introduced = "2.2.0")
public class PlayTTSAction implements IAction {

    /**
     * @inheritDoc
     */
    @Override
    public void runAction(@NotNull Guild guild, String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }

        String text = String.join(" ", arguments);

        RequestUtility.Request request = RequestUtility.Request.builder()
                .url("https://api.streamelements.com/kappa/v2/speech?voice=Brian&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8))
                .GET().build();
        byte[] tts = RequestUtility.requestBytes(request);

        // TODO:: play method, mostly waiting for https://github.com/Walkyst/lavaplayer-fork/pull/45
    }
}
