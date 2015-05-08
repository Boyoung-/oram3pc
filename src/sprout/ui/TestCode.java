package sprout.ui;

import java.math.BigInteger;

public class TestCode {
	public static void main(String[] args) throws Exception {
		System.out.println(BigInteger.valueOf(100).toByteArray().length);
		int one = 1;
		byte[] byte1 = new byte[]{(byte) one};
		System.out.println(byte1[0] == 1);
	}
}
