package sprout.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SR {
	public static SecureRandom rand;
	public static MessageDigest digest;
	
	static {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}