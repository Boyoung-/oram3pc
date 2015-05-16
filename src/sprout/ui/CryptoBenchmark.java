package sprout.ui;

import sprout.crypto.PRF;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.util.StopWatch;

public class CryptoBenchmark {
	public static void main(String[] args) throws Exception {
		StopWatch prf_sw = new StopWatch("PRF");
		StopWatch prg_sw = new StopWatch("PRG");
		StopWatch gc_sw = new StopWatch("GC");

		int toMS = 1000000;
		int iteration = 20000;
		int n_start = 100;
		int n_end = 1000;
		int n_increment = 100;

		byte[] k = new byte[16];
		byte[][] input = new byte[iteration][12];
		byte[][] seed = new byte[iteration][16];

		// prepare input
		for (int i = 0; i < iteration; i++) {
			SR.rand.nextBytes(input[i]);
			SR.rand.nextBytes(seed[i]);
		}

		System.out.println("Each entry of n below shows the cumulative");
		System.out.println("time for doing n*iteration AES/SHA-1 ops");
		System.out.println();
		System.out.println("Iterations\t" + iteration / 2);
		System.out.println(" \tPRF\tPRG\tGC");
		System.out.println("n\t(AES-ECB)\t(AES-CTR)\t(SHA-1)");

		// start timing
		for (int n = n_start; n <= n_end; n += n_increment) {
			SR.rand.nextBytes(k);
			PRF prf = new PRF(128 * n);
			prf.init(k);

			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					prf_sw.start();
				prf.compute(input[i]);
			}
			prf_sw.stop();
			
			PRG prg = new PRG(128 * n);

			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					prg_sw.start();
				prg.compute(seed[i]);
			}
			prg_sw.stop();
			
			for (int i = 0; i < iteration; i++) {
				if (i == iteration / 2)
					gc_sw.start();
				for (int j=0; j<n; j++)
					SR.digest.digest(seed[i]);
			}
			gc_sw.stop();


			System.out.print(n + "\t WC(ms)");
			System.out.print("\t" + prf_sw.elapsedWallClockTime / toMS);
			System.out.print("\t" + prg_sw.elapsedWallClockTime / toMS);
			System.out.print("\t" + gc_sw.elapsedWallClockTime / toMS + "\n");
			System.out.print("\tCPU(ms)");
			System.out.print("\t" + prf_sw.elapsedCPUTime / toMS);
			System.out.print("\t" + prg_sw.elapsedCPUTime / toMS);
			System.out.print("\t" + gc_sw.elapsedCPUTime / toMS + "\n\n");
			
			prf_sw.reset();
			prg_sw.reset();
			gc_sw.reset();
		}

	}

}
