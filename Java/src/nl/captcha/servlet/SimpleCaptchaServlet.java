package nl.captcha.servlet;

import static nl.captcha.Captcha.NAME;

import java.awt.Color;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.gimpy.ShearGimpyRenderer;
import nl.captcha.noise.StraightLineNoiseProducer;


/**
 * Generates, displays, and stores in session a 200x50 CAPTCHA image with sheared black text, 
 * a solid dark grey background, and a straight, slanted red line through the text.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 */
public class SimpleCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static int _width = 200;
    private static int _height = 50;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    	if (getInitParameter("captcha-height") != null) {
    		_height = Integer.valueOf(getInitParameter("captcha-height"));
    	}
    	
    	if (getInitParameter("captcha-width") != null) {
    		_width = Integer.valueOf(getInitParameter("captcha-width"));
    	}
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Captcha captcha = new Captcha.Builder(_width, _height)
        	.addText()
            .addBackground(new FlatColorBackgroundProducer(Color.WHITE))
            .gimp(new ShearGimpyRenderer())
            .addNoise(new StraightLineNoiseProducer())
            .build();

        CaptchaServletUtil.writeImage(resp, captcha.getImage());
        req.getSession().setAttribute(NAME, captcha);
    }
}
