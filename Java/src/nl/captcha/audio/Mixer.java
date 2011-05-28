package nl.captcha.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class Mixer {
    
    public static final InputStream mix(InputStream is1, InputStream is2) throws IOException {
        AudioSampleReader asr1, asr2;
        try {
            asr1 = new AudioSampleReader(is1);
            asr2 = new AudioSampleReader(is2);
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }

        double[] samples1 = asr1.getInterleavedSamples();
        double[] samples2 = asr2.getInterleavedSamples();

        // Prep for mixing.
        // For differing sample rates, need a multiplier so we can get the correct
        // byte from the 2nd byte[].
        float rate1 = asr1.getFormat().getSampleRate();
        float rate2 = asr2.getFormat().getSampleRate();
        float rate_mult = rate2 / rate1;

        // Mix!
        for (int i = 0; i < samples1.length; i++) {
            samples1[i] = (samples1[i] + samples2[(int) (i * rate_mult)]) / 2;
        }

        File tmpFile = File.createTempFile(randFileName(), "wav");
        AudioSampleWriter asw = new AudioSampleWriter(tmpFile,
                asr1.getFormat(), AudioFileFormat.Type.WAVE);
        asw.write(samples1);
        asw.close();

        // TODO: Delete tmpFile

        return new FileInputStream(tmpFile);
    }
    
    private static final String randFileName() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
