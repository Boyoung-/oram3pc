package sprout.crypto;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;

public class BlockCipher {

	private Cipher cipher = null;
	private IvParameterSpec IV = null;

	public BlockCipher(String mode, byte[] IV) throws Exception {
		cipher = Cipher.getInstance(mode, "SunJCE");
		this.IV = new IvParameterSpec(IV);
	}

	public BlockCipher(String mode, String IV) throws Exception {
		cipher = Cipher.getInstance(mode, "SunJCE");
		this.IV = new IvParameterSpec(IV.getBytes("UTF-8"));
	}

	public byte[] encrypt(String pt, String key) throws Exception {
		SecretKeySpec skey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skey, IV);
		return cipher.doFinal(pt.getBytes("UTF-8"));
	}

	public String decrypt(byte[] ct, String key) throws Exception {
		SecretKeySpec skey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, skey, IV);
		return new String(cipher.doFinal(ct), "UTF-8");
	}

	public static void main(String[] args) {
		try {
			String IV = "AAAAAAAAAAAAAAAA";
			String plaintext = "test text 123\0\0\0"; // Note null padding
			String encryptionKey = "0123456789abcdef";

			System.out.println("PT: " + plaintext);

			BlockCipher cipher = new BlockCipher("AES/CBC/NoPadding", IV);
			byte[] ct = cipher.encrypt(plaintext, encryptionKey);

			// debug
			System.out.print("CT: ");
			for (int i = 0; i < ct.length; i++) {
				System.out.print(String.format("%02X", ct[i]));
			}
			System.out.println("");

			String decrypted = cipher.decrypt(ct, encryptionKey);
			System.out.println("PT: " + decrypted);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
