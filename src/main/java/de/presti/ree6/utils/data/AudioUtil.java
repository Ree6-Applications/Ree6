package de.presti.ree6.utils.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A utility class to work with Audio files.
 */
public class AudioUtil {

    /**
     * Constructor for the AudioUtil class.
     */
    private AudioUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get an WAV file out of PCM data.
     * @param inputRawData the PCM data.
     * @return the WAV file.
     * @throws IOException if something goes wrong.
     */
    public static byte[] rawToWave(final byte[] inputRawData) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (DataOutputStream output = new DataOutputStream(byteArrayOutputStream)) {
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + inputRawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, 88200 * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, inputRawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[inputRawData.length / 2];
            ByteBuffer.wrap(inputRawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(inputRawData);
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Write an int to the output stream.
     * @param output the output stream.
     * @param value the value.
     * @throws IOException if something goes wrong.
     */
    private static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    /**
     * Write a short to the output stream.
     * @param output the output stream.
     * @param value the value.
     * @throws IOException if something goes wrong.
     */
    private static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    /**
     * Write a string to the output stream.
     * @param output the output stream.
     * @param value the value.
     * @throws IOException if something goes wrong.
     */
    private static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}
