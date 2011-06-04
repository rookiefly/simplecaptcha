package nl.captcha.text.producer;


/**
 * TextProducer implementation that will return a series of numbers.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class NumberAnswerProducer implements TextProducer {

    private static final int DEFAULT_LENGTH = 5;
    private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9' };

    private final TextProducer _txtProd;
    
    public NumberAnswerProducer() {
        this(DEFAULT_LENGTH);
    }
    
    public NumberAnswerProducer(int length) {
        _txtProd = new DefaultTextProducer(length, NUMBERS);
    }

    @Override public String getText() {
        return new StringBuffer(_txtProd.getText()).toString();
    }
}
