package nl.captcha.audio;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.captcha.audio.noise.RandomNoiseProducer;
import nl.captcha.audio.noise.NoiseProducer;
import nl.captcha.audio.producer.RandomNumberVoiceProducer;
import nl.captcha.audio.producer.VoiceProducer;
import nl.captcha.text.producer.NumberAnswerProducer;
import nl.captcha.text.producer.TextProducer;

public final class AudioCaptcha {

    public static final String NAME = "audioCaptcha";
    private static final Random RAND = new SecureRandom();

    private Builder _builder;

    private AudioCaptcha(Builder builder) {
        _builder = builder;
    }

    public static class Builder {

        private String _answer = "";
        private List<VoiceProducer> _vProds;
        private NoiseProducer _noiseProd;

        public Builder() {
            _vProds = new ArrayList<VoiceProducer>();
        }

        public Builder addAnswer() {
            return addAnswer(new NumberAnswerProducer());
        }

        public Builder addAnswer(TextProducer ansProd) {
            _answer += ansProd.getText();

            return this;
        }

        public Builder addVoice(VoiceProducer vProd) {
            _vProds.add(vProd);

            return this;
        }

        public Builder addNoise() {
            return addNoise(new RandomNoiseProducer());
        }

        public Builder addNoise(NoiseProducer noiseProd) {
            _noiseProd = noiseProd;

            return this;
        }

        public AudioCaptcha build() {
            // Make sure there is at least one voice producer
            if (_vProds.size() == 0) {
                _vProds.add(new RandomNumberVoiceProducer());
            }

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

    public Sample getChallenge() {
        // 1. Convert answer to an array
        char[] ansAry = getAnswer().toCharArray();
        VoiceProducer vProd;
        List<Sample> samples = new ArrayList<Sample>();
        for (int i = 0; i < ansAry.length; i++) {
            vProd = _builder._vProds.get(RAND.nextInt(_builder._vProds.size()));
            samples.add(vProd.getVocalization(ansAry[i]));
        }

        // 2. Append the voices one to the other
        Sample appended = Mixer.append(samples);

        // 3. Add noise
        if (_builder._noiseProd != null) {
            appended = _builder._noiseProd.addNoise(appended);
        }

        return appended;
    }

    @Override public String toString() {
        return _builder.toString();
    }
}
