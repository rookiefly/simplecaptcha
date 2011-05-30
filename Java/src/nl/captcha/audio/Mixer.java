package nl.captcha.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

public final class Mixer {

    private final AudioSampleReader _asr1, _asr2;

    public Mixer(InputStream is1, InputStream is2) throws IOException {

        _asr1 = new AudioSampleReader(is1);
        checkFormat(_asr1);

        _asr2 = new AudioSampleReader(is2);
        checkFormat(_asr2);
    }

    public final InputStream mix() {
        double[] samples1 = _asr1.getInterleavedSamples();
        double[] samples2 = _asr2.getInterleavedSamples();

        // Mix!
        samples1 = mix(samples1, samples2);

        File tmpFile;
        try {
            tmpFile = File.createTempFile(randFileName(), "wav");
            tmpFile.deleteOnExit();
            AudioSampleWriter asw = new AudioSampleWriter(tmpFile,
                    _asr1.getFormat(), AudioFileFormat.Type.WAVE);
            asw.write(samples1);
            asw.close();

            return new FileInputStream(tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Delete tmpFile

        return null;
    }

    public final InputStream append() throws IOException {
        double[] samples1 = _asr1.getInterleavedSamples();
        double[] samples2 = _asr2.getInterleavedSamples();

        double[] appended = new double[samples1.length + samples2.length];
        for (int i = 0; i < samples1.length; i++) {
            appended[i] = samples1[i];
        }

        for (int i = 0; i < samples2.length; i++) {
            appended[samples1.length + i] = samples2[i];
        }

        File tmpFile = File.createTempFile(randFileName(), "wav");
        AudioSampleWriter asw = new AudioSampleWriter(tmpFile,
                _asr1.getFormat(), AudioFileFormat.Type.WAVE);
        asw.write(appended);
        asw.close();

        return new FileInputStream(tmpFile);
    }

    private final double[] mix(double[] sample1, double[] sample2) {
        for (int i = 0; i < sample1.length; i++) {
            if (i >= sample2.length) {
                sample1[i] = 0;
                break;
            }
            sample1[i] = (sample1[i] + sample2[i]);
        }
        return sample1;
    }

    private static final void checkFormat(AudioSampleReader asr)
            throws IOException {
        AudioFormat af = asr.getFormat();
        if (!SupportedAudioFormat.isFormatSupported(af)) {
            throw new IOException("Unsupported audio format.\nReceived: "
                    + af.toString() + "\nExpected: "
                    + SupportedAudioFormat.FORMAT);
        }
    }

    private static final String randFileName() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    private static class SupportedAudioFormat {
        public static final AudioFormat FORMAT = new AudioFormat(
                16000, // sample rate
                16, // sample size in bits
                1, // channels
                true, // signed?
                false); // big endian?;

        public static final boolean isFormatSupported(AudioFormat other) {
            return other.matches(FORMAT);
        }
    }
}
