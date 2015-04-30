package sprout.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

// TODO: rm unused item

public class SR {
	public static SecureRandom rand;
	public static MessageDigest digest;
	public static BigInteger p;
	public static SecretKeySpec skey;
	public static int kBits;
	public static int kBytes;

	static {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
			digest = MessageDigest.getInstance("SHA-1");
			digest.reset();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p =
																			// 2^34
																			// -
																			// 41
		skey = PRG.readKey();
		
		kBits = 80;
		kBytes = (kBits + 7) / 8;
	}
}
