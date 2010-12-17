package nl.captcha.servlet;

import static nl.captcha.Captcha.NAME;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;


/**
 * Builds a 200x50 CAPTCHA and attaches it to the session. This is intended to help prevent
 * bots from simply reloading the page and getting new images until one is
 * generated which they can successfully parse. Removal of the session attribute
 * <code>CaptchaServletUtil.NAME</code> will force a new <code>Captcha</code>
 * to be added to the session.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class StickyCaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 40913456229L;
    
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

    /**
     * Write out the CAPTCHA image stored in the session. If not present,
     * generate a new <code>Captcha</code> and write out its image.
     * 
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Captcha captcha;
        if (session.getAttribute(NAME) == null) {
            captcha = new Captcha.Builder(_width, _height)
            	.addText()
            	.gimp()
            	.addBorder()
                .addNoise()
                .addBackground()
                .build();

            session.setAttribute(NAME, captcha);
            CaptchaServletUtil.writeImage(resp, captcha.getImage());

            return;
        }

        captcha = (Captcha) session.getAttribute(NAME);
        CaptchaServletUtil.writeImage(resp, captcha.getImage());
    }
}
