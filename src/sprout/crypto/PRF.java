package sprout.crypto;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sprout.util.Util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PRF {

	private Cipher cipher = null;
	private int l; // output bit length

	private int maxInputBytes = 12;

	public PRF(int l) {
		try {
			this.cipher = Cipher.getInstance("AES/ECB/NoPadding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		this.l = l;
	}

	public void init(byte[] key) {
		if (key.length != 16)
			try {
				throw new Exception("key length error");
			} catch (Exception e) {
				e.printStackTrace();
			}
		SecretKeySpec skey = new SecretKeySpec(key, "AES");
		try {
			cipher.init(Cipher.ENCRYPT_MODE, skey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	public byte[] compute(byte[] input) {
		if (input.length > maxInputBytes)
			try {
				throw new Exception("input length error: " + input.length
						+ " > " + maxInputBytes);
			} catch (Exception e) {
				e.printStackTrace();
			}

		byte[] output = null;
		if (l <= 128) {
			byte[] in = new byte[16];
			System.arraycopy(input, 0, in, in.length - input.length,
					input.length);
			output = leq128(in, l);
		} else {
			output = greater128(input);
		}

		return output;
	}

	private byte[] leq128(byte[] input, int np) {
		if (input.length != 16)
			try {
				throw new Exception("leq128 input length error");
			} catch (Exception e) {
				e.printStackTrace();
			}

		byte[] ctext = null;
		try {
			ctext = cipher.doFinal(input);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		if (np == 128)
			return ctext;

		byte[] output = Util.getSubBits(new BigInteger(1, ctext), 0, np)
				.toByteArray();

		int outBytes = (np + 7) / 8;
		if (output.length > outBytes)
			output = Util.rmSignBit(output);
		else if (output.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(output, 0, tmp, outBytes - output.length,
					output.length);
			output = tmp;
		}

		return output;
	}

	private byte[] greater128(byte[] input) {
		if (input.length > maxInputBytes)
			try {
				throw new Exception("greater128 input length error");
			} catch (Exception e) {
				e.printStackTrace();
			}

		byte[] in = new byte[16];
		System.arraycopy(input, 0, in, in.length - input.length, input.length);
		int n = l / 128;
		byte[] front = new byte[n * 16];
		for (int i = 0; i < n; i++) {
			byte[] index = BigInteger.valueOf(i + 1).toByteArray();
			System.arraycopy(index, 0, in, 16 - maxInputBytes - index.length,
					index.length);
			byte[] seg = leq128(in, 128);
			System.arraycopy(seg, 0, front, i * seg.length, seg.length);
		}

		int np = l % 128;
		if (np == 0)
			return front;

		byte[] index = BigInteger.valueOf(n + 1).toByteArray();
		System.arraycopy(index, 0, in, 16 - maxInputBytes - index.length,
				index.length);
		byte[] back = leq128(in, np);
		byte[] output = new BigInteger(1, front).shiftLeft(np)
				.or(new BigInteger(1, back)).toByteArray();

		int outBytes = (l + 7) / 8;
		if (output.length > outBytes)
			output = Util.rmSignBit(output);
		else if (output.length < outBytes) {
			byte[] tmp = new byte[outBytes];
			System.arraycopy(output, 0, tmp, outBytes - output.length,
					output.length);
			output = tmp;
		}

		return output;
	}

	// testing
	public static void main(String[] args) {
		try {
			for (int l = 1; l < 5000; l++) {
				System.out.println("Round: l=" + l);
				PRF f1 = new PRF(l);
				PRF f2 = new PRF(l);
				byte[] k = new byte[16];
				SR.rand.nextBytes(k);
				byte[] input = new byte[SR.rand.nextInt(12) + 1];
				SR.rand.nextBytes(input);
				f1.init(k);
				f2.init(k);
				byte[] output1 = f1.compute(input);
				byte[] output2 = f2.compute(input);
				for (int i = 0; i < output2.length; i++)
					System.out.print(String.format("%02X", output2[i]));
				System.out.println("");
				boolean test1 = new BigInteger(1, output1)
						.compareTo(new BigInteger(1, output2)) == 0;
				boolean test2 = output1.length == (l + 7) / 8;
				if (!test1 || !test2) {
					System.out.println("Fail: l=" + l + "  " + test1 + "  "
							+ test2);
					break;
				}
			}

			System.out.println("done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
