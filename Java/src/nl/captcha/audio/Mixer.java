package nl.captcha.audio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Mixer {
    
    public static final InputStream mix(byte[] track1, byte[] track2) {
        
        // TODO: Append zeroes to shorter track before conversion
        
        int[] int_track1 = convert(track1);
        int[] int_track2 = convert(track2);
        
        // Add together & divide by two
        for (int i = 0; i < int_track1.length; i++) {
            int_track1[i] = (int_track1[i] + int_track2[i]) / 2;
        }
        
        byte[] result = convert(int_track1);
        return new ByteArrayInputStream(result);
    }
    
    /**
     * Convert <code>byte[]</code> to <code>int[]</code>. Returns empty <code>int[]</code>
     * if <code>byte_ary</code> is null.
     * 
     * @param byte_ary
     * @return
     */
    public static final int[] convert(byte[] byte_ary) {
        if (null == byte_ary) {
            return new int[0];
        }
        
        int[] int_ary = new int[byte_ary.length];
        for (int i = 0; i < byte_ary.length; i++) {
            int_ary[i] = byte_ary[i];
        }
        
        return int_ary;
    }
    
    /**
     * Convert <code>int[]</code> to <code>byte[]</code>. Returns empty <code>byte[]</code>
     * if <code>int_ary</code> is null.
     * 
     * @param int_ary
     * @return
     */
    public static final byte[] convert(int[] int_ary) {
        if (null == int_ary) {
            return new byte[0];
        }
        
        byte[] byte_ary = new byte[int_ary.length];
        for (int i = 0; i < int_ary.length; i++) {
            byte_ary[i] = (byte) int_ary[i];
        }
        
        return byte_ary;
    }
}
