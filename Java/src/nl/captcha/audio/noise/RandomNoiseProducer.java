package nl.captcha.audio.noise;

import java.security.SecureRandom;
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
    private static final String[] DEFAULT_NOISES = { "/sounds/noises/restaurant.wav",
        "/sounds/noises/zombie.wav",
    };
    
    private final String _noiseFiles[];
    
    public RandomNoiseProducer() {
        this(DEFAULT_NOISES);
    }
    
    public RandomNoiseProducer(String[] noiseFiles) {
        _noiseFiles = noiseFiles;
    }
    
    @Override public Sample addNoise(Sample target) {
        String file = _noiseFiles[RAND.nextInt(_noiseFiles.length)];
        Sample noiseSample = FileUtil.readSample(file);
        
        return Mixer.mix(target, noiseSample);
    }
}
