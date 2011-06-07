package nl.captcha.audio.noise;

import javax.sound.sampled.AudioInputStream;

public interface NoiseProducer {
    public AudioInputStream addNoise(AudioInputStream ais);
}
