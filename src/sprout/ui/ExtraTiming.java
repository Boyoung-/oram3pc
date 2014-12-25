package sprout.ui;

import java.math.BigInteger;

import sprout.crypto.AES_PRF;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.util.StopWatch;
import sprout.util.Util;

public class ExtraTiming
{
    static BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
	
    public static void main(String[] args) throws Exception
    {		
	StopWatch aes_sw = new StopWatch("AES");
	StopWatch prg_string_sw = new StopWatch("PRG_string");
	StopWatch prg_bytes_sw = new StopWatch("PRG_bytes");
	StopWatch pet_sw = new StopWatch("PET");
		
	int iteration = 20000;
	int convert = 1000000;
	byte[] k = new byte[16];
	BigInteger p2 = p.multiply(p);
	byte[][] input = new byte[iteration][];
	byte[][] seed = new byte[iteration][];
	BigInteger[][] mult = new BigInteger[iteration][];
		
	// prepare input
	for (int i=0; i<iteration; i++) {
	    input[i] = new byte[8];
	    seed[i] = new byte[16];
	    SR.rand.nextBytes(input[i]);
	    SR.rand.nextBytes(seed[i]);
	}
		
	for (int n=100; n<=1000; n+=100) {
	    for (int i=0; i<iteration; i++) {
		mult[i] = new BigInteger[4*n];
		for (int j=0; j<4*n; j++) {	
		    mult[i][j] = Util.nextBigInteger(p2);
		}
	    }
	}
		
	System.out.println("Iterations," + iteration/2 + ",(below time is sum for each n)");
	System.out.println("n,,AES,PRG(string),PRG(bytes),PET");
		
	// start timing
	for (int n=100; n<=1000; n+=100) {
	    SR.rand.nextBytes(k);
	    AES_PRF prf = new AES_PRF(128*n);
	    prf.init(k);
	    PRG prg = new PRG(128*n);

	    for (int i=0; i<iteration; i++) {
		if (i == iteration/2)
		    aes_sw.start();
		prf.compute(input[i]);
	    }
	    aes_sw.stop();
				
	    /*
	      for (int i=0; i<iteration; i++) {
	      if (i == iteration/2)
	      prg_string_sw.start();
	      prg.generateBitString(128*n, seed[i]);
	      }
	      prg_string_sw.stop();
	    */
				
	    for (int i=0; i<iteration; i++) {
		if (i == iteration/2)
		    prg_bytes_sw.start();
		//prg.generateBytes(128*n, seed[i]);
		prg.compute(seed[i]);
	    }
	    prg_bytes_sw.stop();

	    for (int i=0; i<iteration; i++) {
		if (i == iteration/2)
		    pet_sw.start();
		for (int j=0; j<4*n; j++) {
		    mult[i][j].mod(p);	
		}
	    }			
	    pet_sw.stop();
			
	    /*
	      aes_sw.divide(iteration);
	      prg_string_sw.divide(iteration);
	      prg_bytes_sw.divide(iteration);
	      pet_sw.divide(iteration);
	    */
			
	    System.out.print(n + ",WC(ms)");
	    System.out.print("," + aes_sw.elapsedWallClockTime/convert);
	    System.out.print("," + prg_string_sw.elapsedWallClockTime/convert);
	    System.out.print("," + prg_bytes_sw.elapsedWallClockTime/convert);
	    System.out.print("," + pet_sw.elapsedWallClockTime/convert + "\n");
	    System.out.print(",CPU(ms)");
	    System.out.print("," + aes_sw.elapsedCPUTime/convert);
	    System.out.print("," + prg_string_sw.elapsedCPUTime/convert);
	    System.out.print("," + prg_bytes_sw.elapsedCPUTime/convert);
	    System.out.print("," + pet_sw.elapsedCPUTime/convert + "\n\n");
			
	    aes_sw.reset();
	    prg_string_sw.reset();
	    prg_bytes_sw.reset();
	    pet_sw.reset();
	}
    }

}
