package nl.captcha.audio;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

public class Mixer {
    public final static InputStream append(List<AudioSampleReader> asrs) {
        int sampleCount = 0;

        // append voices to each other
        double[] first = asrs.get(0).getInterleavedSamples();
        sampleCount += asrs.get(0).getSampleCount();
        
        double[][] samples = new double[asrs.size() - 1][];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = asrs.get(i + 1).getInterleavedSamples();
            sampleCount += asrs.get(i + 1).getSampleCount();
        }

        double[] appended = concatAll(first, samples);

        // convert to byte[]
        byte[] buffer = AudioSampleReader.asByteArray(sampleCount, appended);

        InputStream bais = new ByteArrayInputStream(buffer);
        InputStream ais = new AudioInputStream(bais, SC_AUDIO_FORMAT,
                sampleCount);

        return ais;
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

}
