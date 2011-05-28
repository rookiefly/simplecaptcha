package nl.captcha.audio;

import java.io.*;

import javax.sound.sampled.*;
    
public class AudioSampleWriter implements Runnable {

    private final File _file;
    private final AudioFormat _format;
    private final AudioFileFormat.Type _targetType;
    
    private final PipedOutputStream _pos;
    private final PipedInputStream _pis;
    private final AudioInputStream _ais;

    private byte[] _bytes;

    // Have to use File here because writing to OutputStream exposes a bug
    // in AudioSystem.write...
    public AudioSampleWriter(File file, AudioFormat format,
            AudioFileFormat.Type targetType) throws IOException {
        _file = file;
        _format = format;
        _targetType = targetType;
        
        // Write to the output stream
        _pos = new PipedOutputStream();
        
        // It will then go to the file via the input streams
        _pis = new PipedInputStream(_pos);
        _ais = new AudioInputStream(_pis, format, AudioSystem.NOT_SPECIFIED);
          
        new Thread(this).start();
    }

    public void run() {
        try {
            AudioSystem.write(_ais, _targetType, _file);
        } catch(Exception e) {
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
        int numBytes = sampleCount * (_format.getSampleSizeInBits() / 8);
        if (_bytes == null || numBytes > _bytes.length) {
            _bytes = new byte[numBytes];
        }
        
        // Convert doubles to bytes using _format
        encodeSamples(interleavedSamples, _bytes, sampleCount);
        
        // write it
        _pos.write(_bytes, 0, numBytes);
    }
     
    private void encodeSamples(double[] audioData, byte[] audioBytes,
            int length) {
        int in;
        if (_format.getSampleSizeInBits() == 16) {
            if (_format.isBigEndian()) {
                for (int i = 0; i < length; i++) {
                    in = (int)(audioData[i]*32767);
                    /* First byte is MSB (high order) */
                    audioBytes[2*i] = (byte)(in >> 8);
                    /* Second byte is LSB (low order) */
                    audioBytes[2*i+1] = (byte)(in & 255);
                }
            } else {
                for (int i = 0; i < length; i++) {
                    in = (int)(audioData[i]*32767);
                    /* First byte is LSB (low order) */
                    audioBytes[2*i] = (byte)(in & 255);
                    /* Second byte is MSB (high order) */
                    audioBytes[2*i+1] = (byte)(in >> 8);
                }
            }
        } else if (_format.getSampleSizeInBits() == 8) {
            if (_format.getEncoding().toString().startsWith("PCM_SIGN")) {
                for (int i = 0; i < length; i++) {
                    audioBytes[i] = (byte)(audioData[i]*127);
                }
            } else {
                for (int i = 0; i < length; i++) {
                    audioBytes[i] = (byte)(audioData[i]*127 + 127);
                }
            }
        }
    }
}