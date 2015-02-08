package sprout.oram;

public class PID {
	public static final int size = 13;

	public static final int oprf = 0;
	public static final int decrypt = 1;
	public static final int reshuffle = 2;
	public static final int encrypt = 3;
	public static final int aot = 4;
	public static final int iot = 5;
	public static final int ssot = 6;
	public static final int pet = 7;
	public static final int gcf = 8;
	public static final int access = 9;
	public static final int ppt = 10;
	public static final int eviction = 11;
	
	
	public static final int pre = 12;

	public static final String[] names = { "oprf", "decrypt", "reshuffle",
			"encrypt", "aot", "iot", "ssot", "pet", "gcf", "access", "ppt",
			"eviction", "precomputation" };
}
