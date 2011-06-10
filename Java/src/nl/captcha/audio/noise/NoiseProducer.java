package nl.captcha.audio.noise;

import nl.captcha.audio.Sample;

public interface NoiseProducer {
    public Sample addNoise(Sample target);
}
