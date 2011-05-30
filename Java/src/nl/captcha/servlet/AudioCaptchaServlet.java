package nl.captcha.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.captcha.audio.Mixer;

public class AudioCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 4690256047223360039L;

    @Override protected void doGet(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {

        InputStream is2 = AudioCaptchaServlet.class
                .getResourceAsStream("/sounds/askAquestion.wav");
        InputStream is1 = AudioCaptchaServlet.class
                .getResourceAsStream("/sounds/gridislive.wav");
        Mixer mixer = new Mixer(is1, is2);
        InputStream mixedIs = mixer.append();

        CaptchaServletUtil.writeAudio(response, mixedIs);

        mixedIs.close();
        is2.close();
        is1.close();
    }

    @Override protected void doPost(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
