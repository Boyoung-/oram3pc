package sprout.ui;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sprout.crypto.SR;
import sprout.util.StopWatch;

public class AES_SHA1 {
	public static void main(String[] args) throws Exception {
		int iteration = 20000000;
		int convert = 1000000;
		
		MessageDigest digest = SR.digest;
		byte[] key = new byte[16];
		byte[][] input = new byte[iteration][16];
		SR.rand.nextBytes(key);
		//SR.rand.nextBytes(input);
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		SecretKeySpec skey = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skey);

		StopWatch aes_sw = new StopWatch("AES");
		StopWatch prg_sw = new StopWatch("PRG");

		
		for (int i=0; i<iteration; i++)
			SR.rand.nextBytes(input[i]);

		for (int i = 0; i < iteration; i++) {
			if (i == iteration / 2)
				aes_sw.start();
			cipher.doFinal(input[i]);
		}
		aes_sw.stop();

		for (int i = 0; i < iteration; i++) {
			if (i == iteration / 2)
				prg_sw.start();
			digest.update(input[i]);
			digest.digest();
		}
		prg_sw.stop();

		System.out.print(aes_sw.elapsedWallClockTime / convert);
		System.out.println("\t" + aes_sw.elapsedCPUTime / convert);
		
		System.out.print(prg_sw.elapsedWallClockTime / convert);
		System.out.println("\t" + prg_sw.elapsedCPUTime / convert);
	}

}
