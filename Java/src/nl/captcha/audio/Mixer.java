package nl.captcha.audio;

import static nl.captcha.audio.Sample.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

public class Mixer {
    public final static Sample append(List<Sample> samples) {
        if (samples.size() == 0) {
            return buildSample(0, new double[0]);
        }

        int sampleCount = 0;

        // append voices to each other
        double[] first = samples.get(0).getInterleavedSamples();
        sampleCount += samples.get(0).getSampleCount();

        double[][] samples_ary = new double[samples.size() - 1][];
        for (int i = 0; i < samples_ary.length; i++) {
            samples_ary[i] = samples.get(i + 1).getInterleavedSamples();
            sampleCount += samples.get(i + 1).getSampleCount();
        }

        double[] appended = concatAll(first, samples_ary);

        return buildSample(sampleCount, appended);
    }

    public final static Sample mix(Sample sample1, Sample sample2) {
        double[] s1_ary = sample1.getInterleavedSamples();
        double[] s2_ary = sample2.getInterleavedSamples();

        double[] mixed = mix(s1_ary, s2_ary);

        return buildSample(sample1.getSampleCount(), mixed);
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
        byte[] buffer = Sample.asByteArray(sampleCount, sample);
        InputStream bais = new ByteArrayInputStream(buffer);
        return new AudioInputStream(bais, SC_AUDIO_FORMAT, sampleCount);
    }

    private static final Sample buildSample(long sampleCount, double[] sample) {
        AudioInputStream ais = buildStream(sampleCount, sample);
        return new Sample(ais);
    }
}
