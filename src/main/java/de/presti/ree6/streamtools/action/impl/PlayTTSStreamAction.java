package de.presti.ree6.streamtools.action.impl;

import de.presti.ree6.main.Main;
import de.presti.ree6.streamtools.action.StreamActionInfo;
import de.presti.ree6.streamtools.action.IStreamAction;
import de.presti.ree6.utils.external.RequestUtility;
import de.presti.ree6.utils.others.RandomUtils;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * StreamAction used to play a text-to-speech messages.
 */
@NoArgsConstructor
@StreamActionInfo(name = "PlayTTS", command = "play-tts", description = "Plays a text-to-speech message.", introduced = "2.2.0")
public class PlayTTSStreamAction implements IStreamAction {

    /**
     * @inheritDoc
     */
    @Override
    public void runAction(@NotNull Guild guild, String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return;
        }

        if (!Main.getInstance().getMusicWorker().isConnectedMember(guild.getSelfMember())) return;

        String text = String.join(" ", arguments);

        RequestUtility.Request request = RequestUtility.Request.builder()
                .url("https://api.streamelements.com/kappa/v2/speech?voice=Brian&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8))
                .GET().build();
        byte[] tts = RequestUtility.requestBytes(request);

        Path filePath = Path.of("storage/tmp/", RandomUtils.randomString(16) + ".mp3");

        try {
            Files.write(filePath, tts);
            Main.getInstance().getMusicWorker().loadAndPlay(guild, null, null, filePath.toAbsolutePath().toString(), null, true, false);
        } catch (Exception ignore) {
        }
    }
}
