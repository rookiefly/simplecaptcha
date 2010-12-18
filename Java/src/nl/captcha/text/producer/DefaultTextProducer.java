package nl.captcha.text.producer;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Produces text of a given length from a given array of characters.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class DefaultTextProducer implements TextProducer {

    private static final Random _gen = new SecureRandom();
    private static final int DEFAULT_LENGTH = 5;
    private static final char[] DEFAULT_CHARS = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };
    
    private final int _length;
    private final char[] _srcChars;

    public DefaultTextProducer() {
    	this(DEFAULT_LENGTH, DEFAULT_CHARS);
    }
    
    public DefaultTextProducer(int length, char[] srcChars) {
    	_length = length;
    	_srcChars = Arrays.copyOf(srcChars, srcChars.length);
    }
    
    @Override
    public String getText() {
        int car = _srcChars.length - 1;

        String capText = "";
        for (int i = 0; i < _length; i++) {
            capText += _srcChars[_gen.nextInt(_srcChars.length)];
        }

        return capText;
    }
}
