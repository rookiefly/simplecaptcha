package nl.captcha.audio.producer;

import java.security.SecureRandom;
import java.util.Random;

import nl.captcha.audio.Sample;
import nl.captcha.util.FileUtil;

/**
 * <code>VoiceProducer</code> which generates a vocalization for a given number,
 * randomly selecting from a list of voices. Voices are located in the
 * <code>sounds/en/numbers</code> directory, and have a filename format of
 * <i>num</i>-<i>voice</i>.wav, e.g.: <code>sounds/en/numbers/1-alex.wav</code>.
 *
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 *
 */
public class RandomNumberVoiceProducer implements VoiceProducer {

    private static final Random RAND = new SecureRandom();

    private static final String[] DEFAULT_VOICES = { "alex", "bruce", "fred",
            "ralph", "kathy", "vicki", "victoria" };

    private final String[] _voices;

    public RandomNumberVoiceProducer() {
        this(DEFAULT_VOICES);
    }

    /**
     * Creates a <code>RandomNumberVoiceProducer</code> for the given
     * <code>num</code> and <code>voices</code>.
     *
     * @param voices
     */
    public RandomNumberVoiceProducer(String[] voices) {
        _voices = voices;
    }

    @Override public final Sample getVocalization(char num) {
        try {
            Integer.parseInt(num + "");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Expected <num> to be a number, got '" + num + "' instead.",
                    e);
        }
        
        StringBuilder sb = new StringBuilder("/sounds/en/numbers/");
        sb.append(num);
        sb.append("-");
        sb.append(_voices[RAND.nextInt(_voices.length)]);
        sb.append(".wav");

        return FileUtil.readSample(sb.toString());
    }
}
