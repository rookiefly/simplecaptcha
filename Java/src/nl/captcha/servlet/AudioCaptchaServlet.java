package nl.captcha.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AudioCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 4690256047223360039L;

    @Override protected void doGet(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("audio/mpeg");

        InputStream is = AudioCaptchaServlet.class.getResourceAsStream("/mp3s/barb_mozart1.mp3");
        int readBytes = 0;
        BufferedInputStream buf = new BufferedInputStream(is);
        ServletOutputStream respStream = resp.getOutputStream();
        
        while ((readBytes = buf.read()) != -1) {
            respStream.write(readBytes);
        }
        
        if (respStream != null) {
            respStream.close();
        }
        
        if (buf != null) {
            buf.close();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
