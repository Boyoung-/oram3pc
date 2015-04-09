package sprout.ui;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AEStest {
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] key = new byte[] { 108, -85, -87, 12, -62, -52, -73, -19, -97, 114, 60, -115, -82, 74, -128, 39 };
		byte[] input1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74 };
		byte[] input2 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10 };
		
		//byte[] key = new byte[] { 62, 36, -125, -57, -57, 114, -61, 4, -17, -119, 92, -7, 83, 43, -15, 87 };
		//byte[] input1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 93 };
		//byte[] input2 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29 };
		
		//byte[] key = new byte[] { -9, 101, 31, -83, 28, -29, -28, 114, -111, -19, 22, 57, -49, 90, -36, 1 };
		//byte[] input1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 126 };
		//byte[] input2 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62 };

		SecretKeySpec skey = new SecretKeySpec(key, "AES");
	    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
	    cipher.init(Cipher.ENCRYPT_MODE, skey);

	    byte[] output1 = cipher.doFinal(input1);
	    byte[] output2 = cipher.doFinal(input2);
	    
	    System.out.println(Arrays.toString(output1));
	    System.out.println(Arrays.toString(output2));
	    
	    byte[] a = new byte[]{-1};
	    BigInteger a1 = new BigInteger(a);
	    BigInteger a2 = new BigInteger(1, a);
	    System.out.println(a1);
	    System.out.println(a2);
	}
}
