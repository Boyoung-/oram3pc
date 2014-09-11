// run this program first
// when seeing the prompt for needing the client
// start EvictionTestClient
package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PET
{
	
	public static int executePET(String[] cc, String[] bb) {
		// parameters
    	int n  = cc.length;
    	BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    	
    	// pre-processed inputs
    	BigInteger[] alpha = new BigInteger[n];
    	BigInteger[] beta  = new BigInteger[n];
    	BigInteger[] tau   = new BigInteger[n];
    	BigInteger[] r     = new BigInteger[n];
    	BigInteger[] gama  = new BigInteger[n];
    	BigInteger[] delta = new BigInteger[n];    	
    	
    	// Prof. Jarecki said no worries about seeds yet
    	//int lambda = 1024; // picked; will be fetched from ORAMForest/Tree
    	//byte[] seedC = SecureRandom.getSeed(lambda);
    	//byte[] seedE = SecureRandom.getSeed(lambda);
    	long inclusive = (p.longValue()-1L) - 0L + 1L; // set range: (max - min) + 1
    	
		//SecureRandom rnd = new SecureRandom(seedC); //no worries about seeds
    	SecureRandom rnd = new SecureRandom();
    		    	
    	for (int j=0; j<n; j++) {
    		// alpha_j <- G(s_C)
        	alpha[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % inclusive + 0L); // [0, p-1], Z_p
    	}
    	
    	//rnd.setSeed(seedE); // no worries about seeds
    	for (int j=0; j<n; j++) {
    		// {beta_j, tau_j, r_j} <- G(s_E)
    		beta[j]  = BigInteger.valueOf(Math.abs(rnd.nextLong()) % inclusive + 0L); // [0, p-1], Z_p
    		tau[j]   = BigInteger.valueOf(Math.abs(rnd.nextLong()) % inclusive + 0L); // [0, p-1], Z_p
    		r[j]     = BigInteger.valueOf(Math.abs(rnd.nextLong()) % (inclusive-1L) + 1L); // [1, p-1], Z_p*
    	}
    	
    	for (int j=0; j<n; j++) {
    		// gama_j <- (alpha_j * beta_j - tau_j) mod p
    		gama[j]  = alpha[j].multiply(beta[j]).subtract(tau[j]).mod(p);
    		// delta_j <- (beta_j + r_j) mod p
    		delta[j] = beta[j].add(r[j]).mod(p);
    	}	    	
    	
    	// on-line inputs (simulated by random generation)
    	BigInteger[] c = new BigInteger[n];
    	BigInteger[] b = new BigInteger[n];
    	//rnd.setSeed(System.currentTimeMillis()); // no worries about seeds
    	for (int j=0; j<n; j++) {
    		c[j] = new BigInteger(cc[j], 2);
    		b[j] = new BigInteger(bb[j], 2);
    	}
    	
    	// Protocol	    	
    	// step 2 (party C)
    	BigInteger[] u = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// u_j <- (alpha_j - c_j) mod p
    		u[j] = alpha[j].subtract(c[j]).mod(p);
    	}
    	
    	// step 3 (party E)
    	BigInteger[] w = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// w_j <- (beta_j * u_j - r_j * b_j - tau_j) mod p
    		w[j] = beta[j].multiply(u[j]).subtract(r[j].multiply(b[j])).subtract(tau[j]).mod(p);
    	}
    	
    	// step 4 (party C)
    	BigInteger[] v = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// v_j <- (c_j * delta_j + w_j - gama_j) mod p
    		v[j] = c[j].multiply(delta[j]).add(w[j]).subtract(gama[j]).mod(p);
    		
    		if (v[j].longValue() == 0L) {
    			return j;
    		}
    	}    
    	
    	return -1; // fail to find j_1
	}
		

}
