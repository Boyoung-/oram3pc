package sprout.crypto;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.math.ec.ECPoint;

public class PRG {
	//private MessageDigest digest = SR.digest;
	private Cipher cipher = null;
	private SecretKeySpec skey = SR.skey;
	private int l; // output bit length
	

	public PRG(int l) {
		try {
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.l = l;
	}

	public byte[] compute(ECPoint ecp) {
		return compute(Arrays.copyOfRange(ecp.getEncoded(), 1, 17));
	}

	public byte[] compute(byte[] seed) {
		if (seed.length != 16) {
			try {
				throw new Exception("Wrong seed length!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		IvParameterSpec IV = new IvParameterSpec(seed);
		byte[] msg = new byte[(l + 7) / 8];
		byte[] output = null;
		
		try {
			cipher.init(Cipher.ENCRYPT_MODE, skey, IV);
			output = cipher.doFinal(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return output;
	}
	
	public static void generateKey() {
		byte[] key = new byte[16];
		SR.rand.nextBytes(key);
		SecretKeySpec skey = new SecretKeySpec(key, "AES");
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		
		try {
			fout = new FileOutputStream("keys/aes_prg_key");
			oos = new ObjectOutputStream(fout);
			oos.writeObject(skey);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static SecretKeySpec readKey() {
		FileInputStream fin = null; 
		ObjectInputStream ois = null;
		SecretKeySpec skey = null;
		try {
			fin = new FileInputStream("keys/aes_prg_key");
			ois = new ObjectInputStream(fin);
			skey = (SecretKeySpec) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return skey;
	}
	
	/*
	public byte[] compute(byte[] seed) {
		byte[] output = null;
		if (l <= 160) {
			output = leq160(seed, 0, l);
		} else {
			output = greater160(seed);
		}

		return output;
	}

	private byte[] leq160(byte[] input, int index, int np) {
		digest.update(input);
		if (index > 0)
			digest.update(BigInteger.valueOf(index).toByteArray());
		byte[] ctext = digest.digest();
		if (np == 160)
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

	private byte[] greater160(byte[] input) {
		int n = l / 160;
		byte[] front = new byte[n * 20];
		for (int i = 0; i < n; i++) {
			byte[] seg = leq160(input, i + 1, 160);
			System.arraycopy(seg, 0, front, i * seg.length, seg.length);
		}

		int np = l % 160;
		if (np == 0)
			return front;

		byte[] back = leq160(input, n + 1, np);
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
	*/

	/*
	public String generateBitString(int bits, byte[] seed) {
		return Util.addZero(new BigInteger(1, compute(seed)).toString(2), bits);
	}

	public String generateBitString(int bits, ECPoint seed) {
		return generateBitString(bits, seed.getEncoded());
	}

	public String generateBitString(int bits, BigInteger seed) {
		return generateBitString(bits, seed.toByteArray());
	}
	*/

	public static void main(String[] args) {
		//generateKey();
		
		int n = 10;
		int outBits = 1000;
		int outBytes = (outBits + 7) / 8;
		byte[][] input = new byte[n][16];
		byte[][] output = new byte[n][];
		PRG G = new PRG(outBits);

		for (int i = 0; i < n; i++) {
			SR.rand.nextBytes(input[i]);
			output[i] = G.compute(input[i]);
			System.out.println(new BigInteger(1, output[i]).toString(16));
		}

		for (int i = 0; i < n; i++) {
			byte[] tmp = G.compute(input[i]);
			System.out.println("deterministic:\t" + (new BigInteger(1, tmp).compareTo(new BigInteger(
					1, output[i])) == 0));
			System.out.println("right length:\t" + (output[i].length == outBytes));
		}
	}
}
