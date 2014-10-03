package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CryptoParam
{	
	static SecureRandom rnd = new SecureRandom();
	static BigInteger p = BigInteger.valueOf(953);  // small prime for testing
	static BigInteger q = BigInteger.valueOf(853);  // small prime for testing
	static BigInteger g = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
}
