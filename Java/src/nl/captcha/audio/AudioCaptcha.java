package nl.captcha.audio;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

import nl.captcha.audio.noise.BackgroundConversationNoiseProducer;
import nl.captcha.audio.noise.NoiseProducer;
import nl.captcha.audio.producer.RandomNumberVoiceProducer;
import nl.captcha.audio.producer.VoiceProducer;
import nl.captcha.text.producer.NumberAnswerProducer;
import nl.captcha.text.producer.TextProducer;

public final class AudioCaptcha {

    public static final String NAME = "audioCaptcha";

    private AudioBuilder _builder;

    private AudioCaptcha(AudioBuilder builder) {
        _builder = builder;
    }

    public static class AudioBuilder {

        private String _answer = "";
        private NoiseProducer _noiseProd;

        public AudioBuilder addAnswer() {
            return addAnswer(new NumberAnswerProducer());
        }

        public AudioBuilder addAnswer(TextProducer ansProd) {
            _answer += ansProd.getText();

            return this;
        }

        public AudioBuilder addNoise() {
            return addNoise(new BackgroundConversationNoiseProducer());
        }

        public AudioBuilder addNoise(NoiseProducer noiseProd) {
            _noiseProd = noiseProd;

            return this;
        }

        public AudioCaptcha build() {
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
        return answer.equals(_builder._answer);
    }

    public String getAnswer() {
        return _builder._answer;
    }

    public AudioInputStream getSound() {
        // 1. Convert answer to an array
        char[] ansAry = getAnswer().toCharArray();
        VoiceProducer vProd;
        List<Sample> samples = new ArrayList<Sample>();
        Sample sample;
        for (char c : ansAry) {
            vProd = new RandomNumberVoiceProducer(c);
            sample = new Sample(vProd.getVocalization());
            samples.add(sample);
        }

        // 2. Append the voices one to the other
        AudioInputStream ais = Mixer.append(samples);

        // 3. Add noise
        if (_builder._noiseProd != null) {
            ais = _builder._noiseProd.addNoise(ais);
        }

        return ais;
    }

    @Override public String toString() {
        return _builder.toString();
    }
}
