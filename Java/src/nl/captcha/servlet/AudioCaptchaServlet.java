package nl.captcha.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import nl.captcha.audio.AudioSampleReader;
import nl.captcha.audio.AudioSampleWriter;
import nl.captcha.audio.Mixer;
import nl.captcha.audio.WavFile;

public class AudioCaptchaServlet extends HttpServlet {

    private static final int NUM_BYTES = 150000;
    private static final long serialVersionUID = 4690256047223360039L;

    @Override 
    protected void doGet(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        
        try {
            InputStream is1 = AudioCaptchaServlet.class.getResourceAsStream("/sounds/askAquestion.wav");
            AudioSampleReader asr1 = new AudioSampleReader(is1);
            System.out.println("-=-=-=> asr1.format: " + asr1.getFormat());
            System.out.println("-=-=-=-=> asr1 sampleRate: " + asr1.getFormat().getSampleRate());
            long numSamples1 = asr1.getSampleCount();
            double[] samples1 = new double[(int) numSamples1];
            asr1.getInterleavedSamples(0, numSamples1, samples1);
            
            InputStream is2 = AudioCaptchaServlet.class.getResourceAsStream("/sounds/gridislive.wav");
            AudioSampleReader asr2 = new AudioSampleReader(is2);
            System.out.println("-=-=-=> asr2.format: " + asr2.getFormat());
            System.out.println("-=-=-=-=> asr2 sampleRate: " + asr2.getFormat().getSampleRate());
            long numSamples2 = asr2.getSampleCount();
            double[] samples2 = new double[(int) numSamples2];
            asr2.getInterleavedSamples(0, numSamples2, samples2);

            // Mix!
            // For different sample rates, need multiplier so we can get the right
            // bytes from the 2nd byte array.
            float rate1 = asr1.getFormat().getSampleRate();
            float rate2 = asr2.getFormat().getSampleRate();
            float lcd = rate2 / rate1;
            System.out.println("lcd: " + lcd);
            for (int i = 0; i < samples1.length; i++) {
                samples1[i] = (samples1[i] + samples2[(int) (i * lcd)]) / 2;
            }

            // write to temp file
            File tmpFile = File.createTempFile("temp", "wav");
            AudioSampleWriter asw = new AudioSampleWriter(tmpFile,
                    asr1.getFormat(), AudioFileFormat.Type.WAVE);
            asw.write(samples1);
            asw.close();

            // Convert tempFile to FileInputStream for writing to response
            FileInputStream tmpFis = new FileInputStream(tmpFile);
            CaptchaServletUtil.writeAudio(response, tmpFis);
            
            tmpFis.close();
            tmpFile.delete();
            is1.close();
            is2.close();

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        
//        BufferedInputStream buf1 = new BufferedInputStream(is1);
//        byte[] byte_block1 = new byte[NUM_BYTES];
//        buf1.read(byte_block1, 0, NUM_BYTES);        
//        
//        InputStream is2 = AudioCaptchaServlet.class.getResourceAsStream("/sounds/gridislive.wav");
//        BufferedInputStream buf2 = new BufferedInputStream(is2);
//        byte[] byte_block2 = new byte[NUM_BYTES];
//        buf2.read(byte_block2, 0, NUM_BYTES);
//        
//        InputStream is = Mixer.mix(byte_block1, byte_block2);
//        
//        CaptchaServletUtil.writeAudio(response, is);
    }
    
    @Override
    protected void doPost(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
    
}

