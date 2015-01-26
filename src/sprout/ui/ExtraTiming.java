package sprout.ui;

import java.math.BigInteger;

import sprout.crypto.AES_PRF;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.util.StopWatch;
import sprout.util.Util;

public class ExtraTiming {
	public static void main(String[] args) throws Exception {
		StopWatch aes_sw = new StopWatch("AES");
		StopWatch prg_sw = new StopWatch("PRG");
		StopWatch pet_sw = new StopWatch("PET");

		int iteration = 2000;
		int convert = 1000000;
		int n_start = 200;
		int n_end = 1000;
		int n_increment = 200;
		
		byte[] k = new byte[16];
		BigInteger p2 = SR.p.multiply(SR.p);
		byte[][] input = new byte[iteration][];
		byte[][] seed = new byte[iteration][];
		BigInteger[][] mult = new BigInteger[iteration][];

		// prepare input
		for (int i = 0; i < iteration; i++) {
			input[i] = new byte[8];
			seed[i] = new byte[16];
			SR.rand.nextBytes(input[i]);
			SR.rand.nextBytes(seed[i]);
		}
		
		System.out.println("\nIterations," + iteration / 2
				+ ",(below time is sum for each n)");
		System.out.println("n,,AES,PRG,PET");

		// start timing
		for (int n = n_start; n <= n_end; n += n_increment) {
			SR.rand.nextBytes(k);
			AES_PRF prf = new AES_PRF(128 * n);
			prf.init(k);
			PRG prg = new PRG(128 * n);

			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					aes_sw.start();
				prf.compute(input[i]);
			}
			aes_sw.stop();

			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					prg_sw.start();
				prg.compute(seed[i]);
			}
			prg_sw.stop();
			
			// prepare input
			for (int i = 0; i < iteration; i++) {
				//System.out.println(n + "  " + i);
				mult[i] = new BigInteger[4 * n];
				for (int j = 0; j < 4 * n; j++) {
					mult[i][j] = Util.nextBigInteger(p2);
				}
			}

			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					pet_sw.start();
				for (int j = 0; j < 4 * n; j++) {
					mult[i][j].mod(SR.p);
				}
			}
			pet_sw.stop();

			//aes_sw.divide(iteration);
			//prg_sw.divide(iteration); 
			//pet_sw.divide(iteration);

			System.out.print(n + ",WC(ms)");
			System.out.print("," + aes_sw.elapsedWallClockTime / convert);
			System.out.print("," + prg_sw.elapsedWallClockTime / convert);
			System.out
					.print("," + pet_sw.elapsedWallClockTime / convert + "\n");
			System.out.print(",CPU(ms)");
			System.out.print("," + aes_sw.elapsedCPUTime / convert);
			System.out.print("," + prg_sw.elapsedCPUTime / convert);
			System.out.print("," + pet_sw.elapsedCPUTime / convert + "\n\n");

			aes_sw.reset();
			prg_sw.reset();
			pet_sw.reset();
		}
	
	}

}
