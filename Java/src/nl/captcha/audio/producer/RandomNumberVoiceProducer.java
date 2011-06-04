package nl.captcha.audio.producer;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;

import nl.captcha.audio.AudioSampleReader;

public class RandomNumberVoiceProducer implements VoiceProducer {

    private static final Random RAND = new SecureRandom();
    private static final String[] VOICES = { "alex", "bruce", "fred", "ralph",
            "kathy", "vicki", "victoria" };

    @Override 
    public InputStream getVocalizationOf(String num) {
        InputStream is = RandomNumberVoiceProducer.class.getResourceAsStream(
                "/sounds/en/numbers/2-fred.wav");
        AudioSampleReader asr = new AudioSampleReader(is);

        double[] sample = asr.getInterleavedSamples();
        // convert to byte
        int size = (int) asr.getSampleCount()
                * (SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
        byte[] buffer = new byte[size];

        // convert double[] to byte[]
        int in;
        for (int i = 0; i < sample.length; i++) {
            in = (int) (sample[i] * 32767);
            buffer[2 * i] = (byte) (in & 255);
            buffer[2 * i + 1] = (byte) (in >> 8);
        }

        InputStream bais = new ByteArrayInputStream(buffer);
        InputStream ais = new AudioInputStream(bais, SC_AUDIO_FORMAT, asr.getSampleCount());
        
        return ais;
    }
    
    private static final String getResourceNameForNum(int num) {
        StringBuilder sb = new StringBuilder("/sounds/en/numbers/");
        sb.append(num);
        sb.append("-");
        sb.append(VOICES[RAND.nextInt(VOICES.length)]);
        sb.append(".wav");
        
        return sb.toString();
    }
}
