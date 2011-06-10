package nl.captcha.audio.noise;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import nl.captcha.audio.Mixer;
import nl.captcha.audio.Sample;
import nl.captcha.util.FileUtil;

/**
 * Adds noise to a {@link Sample} from one of the given <code>noiseFiles</code>.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class RandomNoiseProducer implements NoiseProducer {

    private static final Random RAND = new SecureRandom();
    private static final String[] DEFAULT_NOISES = {
            "/sounds/noises/radio_tuning.wav",
            "/sounds/noises/restaurant.wav",
            "/sounds/noises/swimming.wav", };

    private final String _noiseFiles[];
    private final String _noiseFile;

    public RandomNoiseProducer() {
        this(DEFAULT_NOISES);
    }

    public RandomNoiseProducer(String[] noiseFiles) {
        _noiseFiles = noiseFiles;
        _noiseFile = _noiseFiles[RAND.nextInt(_noiseFiles.length)];
    }

    @Override public Sample addNoise(List<Sample> samples) {
        Sample appended = Mixer.append(samples);
        Sample noise = FileUtil.readSample(_noiseFile);

        // Decrease the volume of the noise to make sure the voices can be heard
        return Mixer.mix(appended, 1.0, noise, 0.6);
    }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[File: ");
        sb.append(_noiseFile);
        sb.append("][Files to choose from: ");
        sb.append(_noiseFiles);
        sb.append("]");

        return sb.toString();
    }
}
