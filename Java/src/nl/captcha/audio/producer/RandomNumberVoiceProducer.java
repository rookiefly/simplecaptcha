package nl.captcha.audio.producer;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;

import nl.captcha.audio.AudioSampleReader;

public class RandomNumberVoiceProducer implements VoiceProducer {

    private static final Random RAND = new SecureRandom();

    private static final int DEFAULT_LENGTH = 5;
    private static final String[] DEFAULT_VOICES = { "alex", "bruce", "fred",
            "ralph", "kathy", "vicki", "victoria" };

    private final int _length;
    private final String[] _voices;

    public RandomNumberVoiceProducer() {
        this(DEFAULT_LENGTH, DEFAULT_VOICES);
    }

    public RandomNumberVoiceProducer(int length) {
        this(length, DEFAULT_VOICES);
    }

    public RandomNumberVoiceProducer(int length, String[] voices) {
        _length = length;
        _voices = voices;
    }

    @Override public final InputStream getVocalization() {
        List<AudioSampleReader> asrs = new ArrayList<AudioSampleReader>(_length);
        int sampleCount = 0;

        for (int i = 0; i < _length; i++) {
            InputStream is = randomResource();
            AudioSampleReader asr = new AudioSampleReader(is);
            asrs.add(asr);
            sampleCount += asr.getSampleCount();
        }

        // append voices to each other
        double[] first = asrs.get(0).getInterleavedSamples();
        double[][] samples = new double[asrs.size() - 1][];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = asrs.get(i + 1).getInterleavedSamples();
        }

        double[] appended = concatAll(first, samples);

        // convert to byte[]
        byte[] buffer = AudioSampleReader.asByteArray(sampleCount, appended);

        InputStream bais = new ByteArrayInputStream(buffer);
        InputStream ais = new AudioInputStream(bais, SC_AUDIO_FORMAT,
                sampleCount);

        return ais;
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    private static final double[] concatAll(double[] first, double[]... rest) {
        int totalLength = first.length;
        for (double[] array : rest) {
            totalLength += array.length;
        }
        double[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (double[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private final InputStream randomResource() {
        StringBuilder sb = new StringBuilder("/sounds/en/numbers/");
        sb.append(RAND.nextInt(10));
        sb.append("-");
        sb.append(_voices[RAND.nextInt(_voices.length)]);
        sb.append(".wav");
        System.out.println("[RandomNumberVoiceProducer] resource: "
                + sb.toString());
        return readFileFromJar(sb.toString());
    }
}
