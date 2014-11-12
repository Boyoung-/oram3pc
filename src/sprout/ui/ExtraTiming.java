package sprout.ui;

import java.security.SecureRandom;

import sprout.crypto.AES_PRF;
import sprout.crypto.PRG;
import sprout.util.StopWatch;

public class ExtraTiming
{
	static SecureRandom rnd = new SecureRandom();
	
	public static void main(String[] args) throws Exception
	{		
		int convert = 1000000;
		int iteration = 10000;
		StopWatch aes_sw = new StopWatch("AES");
		StopWatch prg_string_sw = new StopWatch("PRG_string");
		StopWatch prg_bytes_sw = new StopWatch("PRG_bytes");
		byte[] k = new byte[16];
		byte[] input = new byte[8];
		byte[] seed = new byte[16];
		
		System.out.println("Iterations," + iteration);
		System.out.println("n,,AES,PRG(string),PRG(bytes)");
		
		for (int n=15; n<=25; n++) {
			rnd.nextBytes(k);
			AES_PRF prf = new AES_PRF(128*n);
			prf.init(k);
			PRG prg = new PRG(128*n);
			
			for (int i=0; i<iteration; i++) {
				rnd.nextBytes(input);
				rnd.nextBytes(seed);
				
				aes_sw.start();
				prf.compute(input);
				aes_sw.stop();
				
				prg_string_sw.start();
				prg.generateBitString(128*n, seed);
				prg_string_sw.stop();
				
				prg_bytes_sw.start();
				prg.generateBytes(128*n, seed);
				prg_bytes_sw.stop();
			}
			
			System.out.print(n + ",WC(ms)");
			System.out.print("," + aes_sw.elapsedWallClockTime/convert);
			System.out.print("," + prg_string_sw.elapsedWallClockTime/convert);
			System.out.print("," + prg_bytes_sw.elapsedWallClockTime/convert + "\n");
			System.out.print(",CPU(ms)");
			System.out.print("," + aes_sw.elapsedCPUTime/convert);
			System.out.print("," + prg_string_sw.elapsedCPUTime/convert);
			System.out.print("," + prg_bytes_sw.elapsedCPUTime/convert + "\n");
			
			aes_sw.reset();
			prg_string_sw.reset();
			prg_bytes_sw.reset();
		}
	}

}
