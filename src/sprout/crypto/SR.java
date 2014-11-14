package sprout.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SR {
	public static SecureRandom rand;
	static {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}