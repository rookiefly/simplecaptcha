package nl.captcha.audio;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import nl.captcha.audio.producer.RandomNumberVoiceProducer;
import nl.captcha.audio.producer.VoiceProducer;
import nl.captcha.text.producer.NumberAnswerProducer;
import nl.captcha.text.producer.TextProducer;

public final class AudioCaptcha {

    public static final String NAME = "audioCaptcha";

    private Builder _builder;

    private AudioCaptcha(Builder builder) {
        _builder = builder;
    }

    public static class Builder {

        private String _answer = "";
        private InputStream _sound;

        public Builder addAnswer() {
            return addAnswer(new NumberAnswerProducer());
        }

        public Builder addAnswer(TextProducer ansProd) {
            _answer += ansProd.getText();

            return this;
        }

        public AudioCaptcha build() {
            // 1. Convert _answer to an array
            char[] ansAry = _answer.toCharArray();
            VoiceProducer vProd;
            List<AudioSampleReader> asrs = new ArrayList<AudioSampleReader>();
            AudioSampleReader asr;
            for (char c : ansAry) {
                vProd = new RandomNumberVoiceProducer(c);
                asr = new AudioSampleReader(vProd.getVocalization());
                asrs.add(asr);
            }
            _sound = Mixer.append(asrs);

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
    public final static File mixTest(File file1) throws Exception {
        // 0. Get an InputStream for the file
        FileInputStream fis1 = new FileInputStream(file1);

        // 1. new AudioSampleReader
        AudioSampleReader asr1 = new AudioSampleReader(fis1);

        // 2. get the interleaved samples
        double[] sample1 = asr1.getInterleavedSamples();

        // Do things with sample vals

        // WRITE!
        // 3. convert to byte[]
        byte[] buffer = AudioSampleReader.asByteArray(asr1.getSampleCount(),
                sample1);

        // 4. Data is now in buffer[]. Convert to ByteArrayInputStream.
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

        // 5. Convert to AudioInputStream
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

    @Override public String toString() {
        return _builder.toString();
    }

    private static final String randFileName() {
        return "audcap"
                + Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
