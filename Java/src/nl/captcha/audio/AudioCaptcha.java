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

    private Builder _builder;

    private AudioCaptcha(Builder builder) {
        _builder = builder;
    }

    public static class Builder {

        private String _answer = "";
        private NoiseProducer _noiseProd;

        // private AudioInputStream _sound;

        public Builder addAnswer() {
            return addAnswer(new NumberAnswerProducer());
        }

        public Builder addAnswer(TextProducer ansProd) {
            _answer += ansProd.getText();

            return this;
        }

        public Builder addNoise() {
            return addNoise(new BackgroundConversationNoiseProducer());
        }

        public Builder addNoise(NoiseProducer noiseProd) {
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
        return answer.contains(_builder._answer);
    }

    public String getAnswer() {
        return _builder._answer;
    }

    public AudioInputStream getSound() {
        // 1. Convert answer to an array
        char[] ansAry = getAnswer().toCharArray();
        VoiceProducer vProd;
        List<AudioSampleReader> asrs = new ArrayList<AudioSampleReader>();
        AudioSampleReader asr;
        for (char c : ansAry) {
            vProd = new RandomNumberVoiceProducer(c);
            asr = new AudioSampleReader(vProd.getVocalization());
            asrs.add(asr);
        }

        // 2. Append the voices one to the other
        AudioInputStream ais = Mixer.append(asrs);

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
