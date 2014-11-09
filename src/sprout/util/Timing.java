package sprout.util;

public class Timing
{
	private static boolean init = false;
	
	public static StopWatch access;
	public static StopWatch access_online;
	public static StopWatch access_write;
	public static StopWatch access_read;
	
	public static StopWatch decrypt;
	public static StopWatch decrypt_online;
	public static StopWatch decrypt_write;
	public static StopWatch decrypt_read;
	
	public static StopWatch oprf;
	public static StopWatch oprf_online;
	public static StopWatch oprf_write;
	public static StopWatch oprf_read;
	
	public static StopWatch pet;
	public static StopWatch pet_online;
	public static StopWatch pet_write;
	public static StopWatch pet_read;
	
	public static StopWatch aot;
	public static StopWatch aot_online;
	public static StopWatch aot_write;
	public static StopWatch aot_read;
	
	public static StopWatch post;
	public static StopWatch post_online;
	public static StopWatch post_write;
	public static StopWatch post_read;
	
	public static StopWatch reshuffle;
	public static StopWatch reshuffle_online;
	public static StopWatch reshuffle_write;
	public static StopWatch reshuffle_read;
	
	public static StopWatch eviction;
	public static StopWatch eviction_online;
	public static StopWatch eviction_write;
	public static StopWatch eviction_read;
	
	public static StopWatch gcf;
	public static StopWatch gcf_online;
	public static StopWatch gcf_write;
	public static StopWatch gcf_read;
	
	public static StopWatch ssot;
	public static StopWatch ssot_online;
	public static StopWatch ssot_write;
	public static StopWatch ssot_read;
	
	public static StopWatch iot;
	public static StopWatch iot_online;
	public static StopWatch iot_write;
	public static StopWatch iot_read;
	
	public static StopWatch encrypt;
	public static StopWatch encrypt_online;
	public static StopWatch encrypt_write;
	public static StopWatch encrypt_read;
	
	public static void init() {
		init = true;
		
		access = new StopWatch("access");
		access_online = new StopWatch("access_online");
		access_write = new StopWatch("access_write");
		access_read = new StopWatch("access_write");
		
		decrypt = new StopWatch("decrypt");
		decrypt_online = new StopWatch("decrypt_online");
		decrypt_write = new StopWatch("decrypt_write");
		decrypt_read = new StopWatch("decrypt_write");

		oprf = new StopWatch("oprf");
		oprf_online = new StopWatch("oprf_online");
		oprf_write = new StopWatch("oprf_write");
		oprf_read = new StopWatch("oprf_write");
		
		pet = new StopWatch("pet");
		pet_online = new StopWatch("pet_online");
		pet_write = new StopWatch("pet_write");
		pet_read = new StopWatch("pet_write");
		
		aot = new StopWatch("aot");
		aot_online = new StopWatch("aot_online");
		aot_write = new StopWatch("aot_write");
		aot_read = new StopWatch("aot_write");
		
		post = new StopWatch("post");
		post_online = new StopWatch("post_online");
		post_write = new StopWatch("post_write");
		post_read = new StopWatch("post_write");
		
		reshuffle = new StopWatch("reshuffle");
		reshuffle_online = new StopWatch("reshuffle_online");
		reshuffle_write = new StopWatch("reshuffle_write");
		reshuffle_read = new StopWatch("reshuffle_write");
		
		eviction = new StopWatch("eviction");
		eviction_online = new StopWatch("eviction_online");
		eviction_write = new StopWatch("eviction_write");
		eviction_read = new StopWatch("eviction_write");
		
		gcf = new StopWatch("gcf");
		gcf_online = new StopWatch("gcf_online");
		gcf_write = new StopWatch("gcf_write");
		gcf_read = new StopWatch("gcf_write");
		
		ssot = new StopWatch("ssot");
		ssot_online = new StopWatch("ssot_online");
		ssot_write = new StopWatch("ssot_write");
		ssot_read = new StopWatch("ssot_write");
		
		iot = new StopWatch("iot");
		iot_online = new StopWatch("iot_online");
		iot_write = new StopWatch("iot_write");
		iot_read = new StopWatch("iot_write");
		
		encrypt = new StopWatch("encrypt");
		encrypt_online = new StopWatch("encrypt_online");
		encrypt_write = new StopWatch("encrypt_write");
		encrypt_read = new StopWatch("encrypt_write");
	}
	
	public void writeToFile(String filename) {
		if (!init) {
			System.out.println("Timing has not been initialized");
			return;
		}
		
		
	}
	
}
