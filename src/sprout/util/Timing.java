package sprout.util;

public class Timing
{
	public static StopWatch oprf;
	public static StopWatch oprf_online;
	public static StopWatch oprf_write;
	public static StopWatch oprf_read;
	
	public static StopWatch decrypt;
	public static StopWatch decrypt_online;
	public static StopWatch decrypt_write;
	public static StopWatch decrypt_read;
	
	public static StopWatch access;
	public static StopWatch access_online;
	public static StopWatch access_write;
	public static StopWatch access_read;
	
	public static void init() {
		oprf = new StopWatch("oprf");
		oprf_online = new StopWatch("oprf_online");
		oprf_write = new StopWatch("oprf_write");
		oprf_read = new StopWatch("oprf_write");
		
		decrypt = new StopWatch("decrypt");
		decrypt_online = new StopWatch("decrypt_online");
		decrypt_write = new StopWatch("decrypt_write");
		decrypt_read = new StopWatch("decrypt_write");
		
		access = new StopWatch("access");
		access_online = new StopWatch("access_online");
		access_write = new StopWatch("access_write");
		access_read = new StopWatch("access_write");
	}
}
