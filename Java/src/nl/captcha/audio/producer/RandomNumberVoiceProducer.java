package nl.captcha.audio.producer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;

public class RandomNumberVoiceProducer implements VoiceProducer {

    private static final Random RAND = new SecureRandom();

    private static final String[] DEFAULT_VOICES = { "alex", "bruce", "fred",
            "ralph", "kathy", "vicki", "victoria" };

    private final char _num;
    private final String[] _voices;

    public RandomNumberVoiceProducer(char num) {
        this(num, DEFAULT_VOICES);
    }

    public RandomNumberVoiceProducer(char num, String[] voices) {
        try {
            Integer.parseInt(num + "");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Expected <num> to be a number, got '" + num + "' instead.",
                    e);
        }
        _num = num;
        _voices = voices;
    }

    @Override public final InputStream getVocalization() {
        StringBuilder sb = new StringBuilder("/sounds/en/numbers/");
        sb.append(_num);
        sb.append("-");
        sb.append(_voices[RAND.nextInt(_voices.length)]);
        sb.append(".wav");

        return readFileFromJar(sb.toString());
    }

    private static final InputStream readFileFromJar(String filename) {
        InputStream jarIs = RandomNumberVoiceProducer.class
                .getResourceAsStream(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[16384];
        int nRead;

        try {
            while ((nRead = jarIs.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            jarIs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
