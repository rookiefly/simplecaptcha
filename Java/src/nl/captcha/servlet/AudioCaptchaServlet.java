package nl.captcha.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.captcha.audio.AudioCaptcha;

public class AudioCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 4690256047223360039L;

    @Override protected void doGet(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {

        InputStream is2 = AudioCaptchaServlet.class
                .getResourceAsStream("/sounds/en/numbers/1-fred.wav");
        InputStream is1 = AudioCaptchaServlet.class
                .getResourceAsStream("/sounds/en/numbers/2-fred.wav");
        AudioCaptcha mixer = new AudioCaptcha(is1, is2);
        InputStream appendedIs = mixer.append();
        System.out.println("[AudioCaptchaServlet] done appending voices.");

        InputStream noise = AudioCaptchaServlet.class
                .getResourceAsStream("/sounds/noise/restaurant.wav");
        System.out.println("[AudioCaptchaServlet] appending noise to voices.");
        AudioCaptcha mixer2 = new AudioCaptcha(appendedIs, noise);
        InputStream mixedIs = mixer2.mix();

        System.out
                .println("[AudioCaptchaServlet] done appending noise to voices.");

        // CaptchaServletUtil.writeAudio(response, mixedIs);

        appendedIs.close();
        mixedIs.close();
        is2.close();
        is1.close();
    }

    @Override protected void doPost(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
