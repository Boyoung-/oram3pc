package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;

import sprout.crypto.AES_PRF;
import sprout.crypto.PRG;
import sprout.util.StopWatch;
import sprout.util.Util;

public class ExtraTiming
{
	static SecureRandom rnd = new SecureRandom();	

	static BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
	
	public static void main(String[] args) throws Exception
	{		
		StopWatch aes_sw = new StopWatch("AES");
		StopWatch prg_string_sw = new StopWatch("PRG_string");
		StopWatch prg_bytes_sw = new StopWatch("PRG_bytes");
		StopWatch pet_sw = new StopWatch("PET");
		
		int convert = 1000000;
		byte[] k = new byte[16];
		byte[] input = new byte[8];
		byte[] seed = new byte[16];
		
		System.out.println("n,,AES,PRG(string),PRG(bytes),PET");
		
		int n = 100000;
			rnd.nextBytes(k);
			AES_PRF prf = new AES_PRF(128);
			prf.init(k);
			PRG prg = new PRG(128);
			
			for (int i=0; i<n; i++) {
				rnd.nextBytes(input);
				rnd.nextBytes(seed);
				
				aes_sw.start();
				prf.compute(input);
				aes_sw.stop();
				
				prg_string_sw.start();
				prg.generateBitString(128, seed);
				prg_string_sw.stop();
				
				prg_bytes_sw.start();
				prg.generateBytes(128, seed);
				prg_bytes_sw.stop();
				
				for (int j=0; j<4; j++) {
					BigInteger alpha = Util.nextBigInteger(p);
				    BigInteger beta = Util.nextBigInteger(p);
				    BigInteger tau = Util.nextBigInteger(p);
				    BigInteger r = Util.nextBigInteger(p.subtract(BigInteger.ONE)).add(BigInteger.ONE);
				    BigInteger gama  = alpha.multiply(beta).subtract(tau).mod(p);
				    BigInteger delta = beta.add(r).mod(p);
					
					BigInteger c = Util.nextBigInteger(p);
					BigInteger b = Util.nextBigInteger(p);				           
				    
					pet_sw.start();
					BigInteger u = alpha.subtract(c).mod(p);
					BigInteger w = beta.multiply(u).subtract(r.multiply(b)).subtract(tau).mod(p);
					BigInteger v = c.multiply(delta).add(w).subtract(gama).mod(p);				
					pet_sw.stop();
				}
			}
			
			System.out.print(n + ",WC(ms)");
			System.out.print("," + aes_sw.elapsedWallClockTime/convert);
			System.out.print("," + prg_string_sw.elapsedWallClockTime/convert);
			System.out.print("," + prg_bytes_sw.elapsedWallClockTime/convert);
			System.out.print("," + pet_sw.elapsedWallClockTime/convert + "\n");
			System.out.print(",CPU(ms)");
			System.out.print("," + aes_sw.elapsedCPUTime/convert);
			System.out.print("," + prg_string_sw.elapsedCPUTime/convert);
			System.out.print("," + prg_bytes_sw.elapsedCPUTime/convert);
			System.out.print("," + pet_sw.elapsedCPUTime/convert + "\n");
			
			//aes_sw.reset();
			//prg_string_sw.reset();
			//prg_bytes_sw.reset();
	}

}
