package sprout.ui;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class TestCode {
	public static void main(String[] args) throws Exception {
		System.out.println(BigInteger.valueOf(100).toByteArray().length);
		int one = 1;
		byte[] byte1 = new byte[] { (byte) one };
		System.out.println(byte1[0] == 1);

		try {
			// create a string and a byte array
			// String s = "Hello world";

			// create a new RandomAccessFile with filename test
			RandomAccessFile raf = new RandomAccessFile("c:/test/test.txt",
					"rw");
			RandomAccessFile raf2 = new RandomAccessFile("c:/test/test.txt",
					"rw");

			// write something in the file
			// raf.writeUTF(s);

			// set the file pointer at 0 position
			// raf.seek(0);
			byte[] in1 = new byte[1000000000];
			byte[] in2 = new byte[1000000000];

			// create an array equal to the length of raf
			// byte[] arr = new byte[5];

			// read the file
			raf.seek(0);
			raf.read(in1, 0, in1.length);

			System.out.println("111");

			// byte[] arr2 = new byte[5];

			raf2.seek(0);
			raf2.read(in2, 0, in2.length);
			System.out.println("222");

			// raf.seek(1);
			// raf.write(arr2, 0, arr2.length);

			// create a new string based on arr
			// String s2 = new String(arr);

			// print it
			// System.out.println(new String(arr));
			// System.out.println(new String(arr2));

			raf.close();
			raf2.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
