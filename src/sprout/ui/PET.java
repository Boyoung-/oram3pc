package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PET
{	
	static SecureRandom rnd = new SecureRandom();
	
	public static int executePET(String[] cc, String[] bb) {
		// parameters
		int m = 32;
    	int n  = cc.length;
    	BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    	
    	// pre-computed inputs
    	// party D
    	BigInteger[] alpha = new BigInteger[n];
    	BigInteger[] beta  = new BigInteger[n];
    	BigInteger[] tau   = new BigInteger[n];
    	BigInteger[] r     = new BigInteger[n];
    	BigInteger[] gama  = new BigInteger[n];
    	BigInteger[] delta = new BigInteger[n];      		    	
    	for (int j=0; j<n; j++) {
        	alpha[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
    	}    	
    	for (int j=0; j<n; j++) {
    		beta[j]  = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
    		tau[j]   = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
    		r[j]     = BigInteger.valueOf(Math.abs(rnd.nextLong()) % (p.longValue()-1L) + 1L); // [1, p-1], Z_p*
    	}    	
    	for (int j=0; j<n; j++) {
    		// gama_j <- (alpha_j * beta_j - tau_j) mod p
    		gama[j]  = alpha[j].multiply(beta[j]).subtract(tau[j]).mod(p);
    		// delta_j <- (beta_j + r_j) mod p
    		delta[j] = beta[j].add(r[j]).mod(p);
    	}	    	
    	// D sends alpha, gama, delta to C
    	// D sends beta, tau, r to E
    	
    	
    	// on-line inputs
    	BigInteger[] c = new BigInteger[n];
    	BigInteger[] b = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		c[j] = new BigInteger(cc[j], 2);
    		b[j] = new BigInteger(bb[j], 2);
    	}
    	
    	// Protocol	    	
    	// step 1 
    	// party C
    	BigInteger[] u = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// u_j <- (alpha_j - c_j) mod p
    		u[j] = alpha[j].subtract(c[j]).mod(p);
    	}
    	// C sends u to E
    	
    	// step 3 
    	// party E
    	BigInteger[] w = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// w_j <- (beta_j * u_j - r_j * b_j - tau_j) mod p
    		w[j] = beta[j].multiply(u[j]).subtract(r[j].multiply(b[j])).subtract(tau[j]).mod(p);
    	}
    	// E sends w to C
    	
    	// step 4 
    	// party C
    	BigInteger[] v = new BigInteger[n];
    	for (int j=0; j<n; j++) {
    		// v_j <- (c_j * delta_j + w_j - gama_j) mod p
    		v[j] = c[j].multiply(delta[j]).add(w[j]).subtract(gama[j]).mod(p);
    		
    		if (v[j].longValue() == 0L) {
    			// C outputs j s.t. v[j] = 0
    			return j;
    		}
    	}    
    	
    	return -1; // fail to find j
	}
		

}
