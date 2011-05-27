package nl.captcha.audio;

import org.junit.Test;

import static nl.captcha.audio.Mixer.*;
import static org.junit.Assert.*;

public class MixerTest {
    @Test
    public void convertTest() {
        byte[] byte_ary = {0, 1, 2, 3};
        int[] int_ary = convert(byte_ary);
        assertEquals(0, int_ary[0]);
        assertEquals(1, int_ary[1]);
        assertEquals(2, int_ary[2]);
        assertEquals(3, int_ary[3]);
        
        int_ary = convert((byte[]) null);
        assertEquals(0, int_ary.length);
        
        byte_ary = convert((int[]) null);
        assertEquals(0, byte_ary.length);
    }
}
