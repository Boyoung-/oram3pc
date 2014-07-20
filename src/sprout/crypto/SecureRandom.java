package sprout.crypto;

public class SecureRandom
{
	// TODO: implement secure random functions here
	// these will be changed to proper implementations when we get to that point!
	
	public static byte[] generateRandomBytes(int bytes)
	{
		return Random.generateRandomBytes(bytes);
	}
	
	public static int generateRandomInt(int low, int high)
	{
		return Random.generateRandomInt(low, high);
	}
	
	public static long generateRandomLong(long low, long high)
	{
		return Random.generateRandomLong(low, high);
	}
	
	public static byte generateRandomBit()
	{
		return (byte)Random.generateRandomInt(0, 1);
	}
}
