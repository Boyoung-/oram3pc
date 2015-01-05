package sprout.oram;

public class TID {
	public static final int size = 6;

	public static final int online = 0;
	public static final int online_write = 1;
	public static final int online_read = 2;
	public static final int offline = 3;
	public static final int offline_write = 4;
	public static final int offline_read = 5;

	public static final String[] names = { "online", "online_write",
			"online_read", "offline", "offline_write", "offline_read" };
}
