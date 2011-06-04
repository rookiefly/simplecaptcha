package nl.captcha.audio;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import nl.captcha.audio.producer.RandomNumberVoiceProducer;
import nl.captcha.audio.producer.VoiceProducer;
import nl.captcha.text.producer.NumberAnswerProducer;
import nl.captcha.text.producer.TextProducer;

public final class AudioCaptcha {
    private Builder _builder;
    private AudioSampleReader _asr1, _asr2;

    private AudioCaptcha(Builder builder) {
        _builder = builder;
    }

    public static class Builder {
        private static final Random RAND = new SecureRandom();

        private String _answer = "";
        private InputStream _sound;
        private List<VoiceProducer> _voiceProds = new ArrayList<VoiceProducer>();

        public Builder addAnswer() {
            return addAnswer(new NumberAnswerProducer());
        }

        public Builder addAnswer(TextProducer ansProd) {
            _answer += ansProd.getText();

            return this;
        }

        public Builder addVoice() {
            return addVoice(new RandomNumberVoiceProducer());
        }

        public Builder addVoice(VoiceProducer voiceProd) {
            _voiceProds.add(voiceProd);

            return this;
        }

        public AudioCaptcha build() {
            // 1. Convert _answer to an array
            char[] ansAry = _answer.toCharArray();

            // 2. Get a random element from the list of voice producers
            VoiceProducer vProd = _voiceProds.get(RAND.nextInt(_voiceProds
                    .size()));
            _sound = vProd.getVocalizationOf("1");

            return new AudioCaptcha(this);
        }

        @Override public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[Answer: ");
            sb.append(_answer);
            sb.append("]");

            return sb.toString();
        }
    }

    public boolean isCorrect(String answer) {
        return answer.contains(_builder._answer);
    }

    public String getAnswer() {
        return _builder._answer;
    }

    public InputStream getSound() {
        return _builder._sound;
    }

    // Below here is stuff for reference during Builder pattern transition.
    // Will eventually be removed.

    public AudioCaptcha(InputStream is1, InputStream is2) throws IOException {
        _asr1 = new AudioSampleReader(is1);
        _asr2 = new AudioSampleReader(is2);
    }

    public final InputStream mix() {
        double[] samples1 = _asr1.getInterleavedSamples();
        double[] samples2 = _asr2.getInterleavedSamples();

        // Mix!
        samples1 = mix(samples1, samples2);

        return writeSample(samples1);
    }

    public final static File mixTest(File file1, File file2) throws Exception {
        // 0. Get an InputStream for the file
        FileInputStream fis1 = new FileInputStream(file1);

        // 1. new AudioSampleReader
        AudioSampleReader asr1 = new AudioSampleReader(fis1);

        // 2. get the interleaved samples
        double[] sample1 = asr1.getInterleavedSamples();

        // Do things with sample vals

        // WRITE!
        // 3. convert to byte[]
        // 3a. get size the byte[] needs to be
        int size = (int) asr1.getSampleCount()
                * (SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
        byte[] buffer = new byte[size];

        // 3b. convert double[] to byte[]
        int in;
        for (int i = 0; i < sample1.length; i++) {
            in = (int) (sample1[i] * 32767);
            // First byte is in LSB
            buffer[2 * i] = (byte) (in & 255);
            buffer[2 * i + 1] = (byte) (in >> 8);
        }

        // 4. Data is now in buffer[]. Convert to ByteArrayInputStream.
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

        // Convert to AudioInputStream
        AudioInputStream ais = new AudioInputStream(bais, SC_AUDIO_FORMAT,
                asr1.getSampleCount());

        File tmpFile = File.createTempFile(randFileName(), ".wav");
        FileOutputStream fos = new FileOutputStream(tmpFile);

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, fos);

        fis1.close();
        bais.close();
        ais.close();
        fos.flush();
        fos.close();

        return tmpFile;
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

        return writeSample(appended);
    }

    private static final double[] mix(double[] sample1, double[] sample2) {
        for (int i = 0; i < sample1.length; i++) {
            if (i >= sample2.length) {
                sample1[i] = 0;
                break;
            }
            sample1[i] = (sample1[i] + sample2[i]);
        }
        return sample1;
    }

    public final InputStream writeSample(double[] samples1) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile(randFileName(), ".wav");
            tmpFile.deleteOnExit();

            AudioSampleWriter asw = new AudioSampleWriter(tmpFile);
            asw.write(samples1);
            asw.close();

            return new FileInputStream(tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public String toString() {
        return _builder.toString();
    }

    private static final String randFileName() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
