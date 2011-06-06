package nl.captcha.audio;

import java.io.*;

import javax.sound.sampled.*;

public class AudioSampleReader {

    public static final AudioFormat SC_AUDIO_FORMAT = new AudioFormat(
            16000, // sample rate
            16, // sample size in bits
            1, // channels
            true, // signed?
            false); // big endian?;

    private final AudioInputStream _audioInputStream;
    private final AudioFormat _format;

    public AudioSampleReader(InputStream is) {
        if (is instanceof AudioInputStream) {
            _audioInputStream = (AudioInputStream) is;
            _format = _audioInputStream.getFormat();
            return;
        }

        try {
            _audioInputStream = AudioSystem.getAudioInputStream(is);
            _format = _audioInputStream.getFormat();
            checkFormat(_format);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AudioFormat getFormat() {
        return _format;
    }

    /**
     * Return the number of samples of all channels
     * 
     * @return
     */
    public long getSampleCount() {
        long total = (_audioInputStream.getFrameLength()
                * _format.getFrameSize() * 8)
                / _format.getSampleSizeInBits();
        return total / _format.getChannels();
    }

    public double[] getInterleavedSamples() {
        double[] samples = new double[(int) getSampleCount()];
        try {
            getInterleavedSamples(0, getSampleCount(), samples);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return samples;
    }

    /**
     * Get the interleaved decoded samples for all channels, from sample index
     * <code>begin</code> (included) to sample index <code>end</code> (excluded)
     * and copy them into <code>samples</code>. <code>end</code> must not exceed
     * <code>getSampleCount()</code>, and the number of samples must not be so
     * large that the associated byte array cannot be allocated
     * 
     * @param begin
     * @param end
     * @param samples
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public double[] getInterleavedSamples(long begin, long end, double[] samples)
            throws IOException, IllegalArgumentException {
        long nbSamples = end - begin;
        long nbBytes = nbSamples * (_format.getSampleSizeInBits() / 8)
                * _format.getChannels();
        if (nbBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Too many samples. Try using a smaller wav.");
        }
        // allocate a byte buffer
        byte[] inBuffer = new byte[(int) nbBytes];
        // read bytes from audio file
        _audioInputStream.read(inBuffer, 0, inBuffer.length);
        // decode bytes into samples.
        decodeBytes(inBuffer, samples);

        return samples;
    }

    /**
     * Extract samples of a particular channel from interleavedSamples and copy
     * them into channelSamples
     * 
     * @param channel
     * @param interleavedSamples
     * @param channelSamples
     */
    public void getChannelSamples(int channel, double[] interleavedSamples,
            double[] channelSamples) {
        int nbChannels = _format.getChannels();
        for (int i = 0; i < channelSamples.length; i++) {
            channelSamples[i] = interleavedSamples[nbChannels * i + channel];
        }
    }

    /**
     * Convenience method. Extract left and right channels for common stereo
     * files. leftSamples and rightSamples must be of size getSampleCount()
     * 
     * @param leftSamples
     * @param rightSamples
     * @throws IOException
     */
    public void getStereoSamples(double[] leftSamples, double[] rightSamples)
            throws IOException {
        long sampleCount = getSampleCount();
        double[] interleavedSamples = new double[(int) sampleCount * 2];
        getInterleavedSamples(0, sampleCount, interleavedSamples);
        for (int i = 0; i < leftSamples.length; i++) {
            leftSamples[i] = interleavedSamples[2 * i];
            rightSamples[i] = interleavedSamples[2 * i + 1];
        }
    }

    // Decode bytes of audioBytes into audioSamples
    public void decodeBytes(byte[] audioBytes, double[] audioSamples) {
        int sampleSizeInBytes = _format.getSampleSizeInBits() / 8;
        int[] sampleBytes = new int[sampleSizeInBytes];
        int k = 0; // index in audioBytes
        for (int i = 0; i < audioSamples.length; i++) {
            // collect sample byte in big-endian order
            if (_format.isBigEndian()) {
                // bytes start with MSB
                for (int j = 0; j < sampleSizeInBytes; j++) {
                    sampleBytes[j] = audioBytes[k++];
                }
            } else {
                // bytes start with LSB
                for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
                    sampleBytes[j] = audioBytes[k++];
                    if (sampleBytes[j] != 0)
                        j = j + 0;
                }
            }
            // get integer value from bytes
            int ival = 0;
            for (int j = 0; j < sampleSizeInBytes; j++) {
                ival += sampleBytes[j];
                if (j < sampleSizeInBytes - 1)
                    ival <<= 8;
            }
            // decode value
            double ratio = Math.pow(2., _format.getSampleSizeInBits() - 1);
            double val = ((double) ival) / ratio;
            audioSamples[i] = val;
        }
    }

    @Override public String toString() {
        return "[AudioSampleReader] samples: " + getSampleCount()
                + ", format: " + _format;
    }

    /**
     * Helper method to convert a double[] to a byte[] in a format that can be
     * used by <code>AudioInputStream</code>. Typically this will be used with a
     * <code>sample</code> that has been modified from its original.
     * 
     * @see <a href="http://en.wiktionary.org/wiki/yak_shaving">Yak Shaving</a>
     * 
     * @return
     */
    public static final byte[] asByteArray(long sampleCount, double[] sample) {
        int b_len = (int) sampleCount
                * (SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
        byte[] buffer = new byte[b_len];

        int in;
        for (int i = 0; i < sample.length; i++) {
            in = (int) (sample[i] * 32767);
            buffer[2 * i] = (byte) (in & 255);
            buffer[2 * i + 1] = (byte) (in >> 8);
        }

        return buffer;
    }

    private static final void checkFormat(AudioFormat af) throws IOException {
        if (!af.matches(SC_AUDIO_FORMAT)) {
            throw new IOException("Unsupported audio format.\nReceived: "
                    + af.toString() + "\nExpected: " + SC_AUDIO_FORMAT);

        }
    }
}
