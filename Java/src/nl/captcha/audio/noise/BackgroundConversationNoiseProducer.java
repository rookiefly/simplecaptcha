package nl.captcha.audio.noise;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;

import nl.captcha.audio.Sample;
import nl.captcha.audio.Mixer;
import nl.captcha.util.FileUtil;

public class BackgroundConversationNoiseProducer implements NoiseProducer {

    @Override public AudioInputStream addNoise(AudioInputStream ais) {
        Sample sample = new Sample(ais);

        InputStream noiseIs = FileUtil
                .readResource("/sounds/noises/restaurant.wav");
        Sample noiseSample = new Sample(noiseIs);

        return Mixer.mix(sample, noiseSample);
    }

}
