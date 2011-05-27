package nl.captcha.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public final class CaptchaServletUtil {

    public static void writeImage(HttpServletResponse response, BufferedImage bi) {
        response.setHeader("Cache-Control", "private,no-cache,no-store");
        response.setContentType("image/png");	// PNGs allow for transparency. JPGs do not.
        try {
            writeImage(response.getOutputStream(), bi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeImage(OutputStream os, BufferedImage bi) {
    	try {
			ImageIO.write(bi, "png", os);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void writeAudio(HttpServletResponse response, InputStream buf) throws IOException {
        response.setHeader("Cache-Control", "private,no-cache,no-store");
        response.setContentType("audio/x-wav");
        
        OutputStream os = response.getOutputStream();
        
        int readBytes = 0;
        while ((readBytes = buf.read()) != -1) {
            os.write(readBytes);
        }
        
        if (os != null) {
            os.close();
        }
        
        if (buf != null) {
            buf.close();
        }
    }
}
