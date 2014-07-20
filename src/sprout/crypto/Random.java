package sprout.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Random
{	
	
	// TODO: don't create a new instance each time, but keep a counter internally about when to re-seed
	
	public static byte[] generateRandomBytes(int bytes)
	{
		SecureRandom rand = null;
		try
		{
			rand = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		byte[] b = new byte[bytes];
		rand.nextBytes(b);
		return b;
	}
	
	public static int generateRandomInt(int low, int high)
	{
		SecureRandom rand = null;
		try
		{
			rand = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		int val = 0;
		val = rand.nextInt(high - low + 1) + low;
		while (val < 0)
		{
			val = rand.nextInt(high - low + 1) + low;
		}
		return val;
	}
	
	public static long generateRandomLong(long low, long high)
	{
		SecureRandom rand = null;
		try
		{
			rand = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		long val = 0;
		val = (rand.nextLong() % (high - low + 1)) + low;
		while (val < 0)
		{
			val = (rand.nextLong() % (high - low + 1)) + low;
		}
		return val;
	}
}
