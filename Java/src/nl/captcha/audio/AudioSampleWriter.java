package nl.captcha.audio;

import java.io.*;

import javax.sound.sampled.*;

import static nl.captcha.audio.AudioSampleReader.SC_AUDIO_FORMAT;

public class AudioSampleWriter implements Runnable {

    private File _file;

    private PipedOutputStream _pos;
    private PipedInputStream _pis;
    private AudioInputStream _ais;
    
    private byte[] _bytes;

    public AudioSampleWriter(File file)
            throws IOException {
        _file = file;

        // Write to the output stream
        _pos = new PipedOutputStream();

        // It will then go to the file via the input streams
        _pis = new PipedInputStream(_pos);
        _ais = new AudioInputStream(_pis, SC_AUDIO_FORMAT,
                AudioSystem.NOT_SPECIFIED);

        new Thread(this).start();
    }

    public void run() {
        try {
            AudioSystem.write(_ais, AudioFileFormat.Type.WAVE, _file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(double[] interleavedSamples) throws IOException {
        writeInterleavedSamples(interleavedSamples, interleavedSamples.length);
    }

    public void close() throws IOException {
        if (_pos != null) {
            _ais.close();
            _pis.close();
            _pos.close();
        }
    }

    private void writeInterleavedSamples(double[] interleavedSamples,
            int sampleCount) throws IOException {
        // Allocate a new bytes array if necessary. If bytes is too long,
        // don't worry about it, just use as much as is needed.
        int numBytes = sampleCount
                * (SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
        if (_bytes == null || numBytes > _bytes.length) {
            _bytes = new byte[numBytes];
        }

        encodeSamples(interleavedSamples, _bytes, sampleCount);

        // write it
        _pos.write(_bytes, 0, numBytes);
    }

    // TODO: Make private
    public static void encodeSamples(double[] audioData, byte[] audioBytes, int length) {
        int in;
        for (int i = 0; i < length; i++) {
            in = (int) (audioData[i] * 32767);
            /* First byte is LSB (low order) */
            audioBytes[2 * i] = (byte) (in & 255);
            /* Second byte is MSB (high order) */
            audioBytes[2 * i + 1] = (byte) (in >> 8);
        }
    }
}