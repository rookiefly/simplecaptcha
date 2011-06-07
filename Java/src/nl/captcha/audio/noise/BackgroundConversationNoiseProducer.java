package nl.captcha.audio.noise;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;

import nl.captcha.audio.AudioSampleReader;
import nl.captcha.audio.Mixer;
import nl.captcha.util.FileUtil;

public class BackgroundConversationNoiseProducer implements NoiseProducer {

    @Override public AudioInputStream addNoise(AudioInputStream ais) {
        AudioSampleReader asr = new AudioSampleReader(ais);

        InputStream noiseIs = FileUtil
                .readResource("/sounds/noises/restaurant.wav");
        AudioSampleReader noiseAsr = new AudioSampleReader(noiseIs);

        return Mixer.mix(asr, noiseAsr);
    }

}
