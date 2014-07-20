package sprout.util;

import java.math.BigInteger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Util
{

	public static FileHandler fh = null;
	public static Logger LOG = initLog();

	// Flag to toggle output of debug print statements
	public static boolean debugEnabled = true;

	public static Logger initLog()
	{
		Logger log = Logger.getLogger("");

		log.setLevel(Level.ALL);

		// Remove the console logger
		for (Handler h : log.getHandlers())
		{
			log.removeHandler(h);
		}

		return log;
	}

	public static void setLogFile(String file)
	{
		if (fh != null)
		{
			LOG.removeHandler(fh);
		}

		try
		{
			fh = new FileHandler(file, false);
		}
		catch (Exception e)
		{

			e.printStackTrace();
			return;
		}

		fh.setFormatter(new SimpleFormatter());

		LOG.addHandler(fh);
	}

	public static byte[] longToByteArray(long n)
	{
		return (byte[]) ByteBuffer.allocate(Long.SIZE / 8).putLong(n).rewind().array();
	}

	public static long byteArrayToLong(byte[] n)
	{
		// Fill out the byte array to make it <long byte number> long
		byte[] full = new byte[Long.SIZE / 8];
		int offset = 0;
		for (int i = 0; i < full.length - n.length; i++)
		{
			full[offset++] = 0x00;
		}
		for (int i = 0; i < n.length; i++)
		{
			full[offset++] = n[i];
		}

		ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / 8);
		bb.put(full);
		bb.rewind();
		return bb.getLong();
	}

	public static String byteArrayToString(byte[] ba)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ba.length; i++)
		{
			builder.append(Integer.toHexString(ba[i]));
		}
		return builder.toString();
	}

	public static String toKaryString(long l, int k, int width)
	{
		String karyRep = Long.toString(l, k);
		if (karyRep.length() < (width + 1))
		{
			StringBuilder builder = new StringBuilder(karyRep);
			while (builder.length() != width)
			{
				builder.insert(0, "0");
			}
			karyRep = builder.toString();
		}
		
		assert(karyRep.length() == width);
		
		return karyRep;
	}

	public static String byteArraytoKaryString(byte[] bytes, int k)
	{
		BigInteger bi = new BigInteger(bytes);
		return bi.toString(k);
	}
	
	public static String byteArraytoKaryString(byte[] bytes, int k, int width)
	{
		BigInteger bi = new BigInteger(bytes);
		String rep = bi.toString(k);
		if (rep.length() < width)
		{
			while (rep.length() != width)
			{
				rep = "0" + rep;
			}
		}
		
		assert(rep.length() == width);
		
		return rep;
	}
	
	public static byte[] bitsToByteArray(String bits)
	{
		BigInteger bi = new BigInteger(bits, 2);
		return bi.toByteArray();
	}

	public static void error(String m)
	{
		System.err.println(m);
		LOG.severe(m);
	}

	public static void error(String m, Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		LOG.severe(m + "\n" + e.getMessage() + "\n" + sw);
	}

	public static void disp(String m)
	{
		System.out.println(m);
		LOG.info(m);
	}
	
	public static void disp(byte[] b)
	{
		System.out.print("[");
		for (int i = 0; i < b.length - 2; i++)
		{
			System.out.print(b[i] + " ");
		}
		System.out.println("" + b[b.length - 1] + "]");
	}

	public static void warn(String m)
	{
		LOG.warning(m);
	}

	public static void debug(String m)
	{
		LOG.fine(m);
	}
}
