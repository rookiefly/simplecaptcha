package nl.captcha.audio;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

public class Mixer {
    public final static AudioInputStream append(List<AudioSampleReader> asrs) {
        if (asrs.size() == 0) {
            return buildStream(0, new double[0]);
        }

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

        return buildStream(sampleCount, appended);
    }

    public final static AudioInputStream mix(AudioSampleReader asr1,
            AudioSampleReader asr2) {
        double[] sample1 = asr1.getInterleavedSamples();
        double[] sample2 = asr2.getInterleavedSamples();

        double[] mixed = mix(sample1, sample2);

        return buildStream(asr1.getSampleCount(), mixed);
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

    private static final double[] mix(double[] sample1, double[] sample2) {
        for (int i = 0; i < sample1.length; i++) {
            if (i >= sample2.length) {
                sample1[i] = 0;
                break;
            }
            sample1[i] = (sample1[i] + (sample2[i] * 2));
        }
        return sample1;
    }

    private static final AudioInputStream buildStream(long sampleCount,
            double[] sample) {
        byte[] buffer = AudioSampleReader.asByteArray(sampleCount, sample);
        InputStream bais = new ByteArrayInputStream(buffer);
        return new AudioInputStream(bais, SC_AUDIO_FORMAT, sampleCount);
    }
}
