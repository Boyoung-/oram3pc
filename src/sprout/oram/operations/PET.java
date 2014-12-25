package sprout.oram.operations;

import java.math.BigInteger;
import java.util.Arrays;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.util.Util;

import org.apache.commons.lang3.NotImplementedException;

// TODO: PET doesn't need all of the paramaters that the other operations have, can we make it more generic?
public class PET extends Operation {
  
    /* TODO: Work out how to do precomputation
    // We may want to extend operation and put that in there, not sure
    public void precompute(Party party) {
    switch (party) {
    case Charlie:
    break;forest
    case Debbie:
    break;
    case Eddie:
    break;
    }
    }
    public void setDebbiePrecomputation() {
    
    }
    */

    private static BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
  
    private static boolean D = false;
  
    public PET(Communication con1, Communication con2) {
	super(con1, con2);
    }

    public void executeDebbie(Communication charlie, Communication eddie, int n) {
	charlie.countBandwidth = false;
	eddie.countBandwidth = false;
	  
	// Debbie does nothing online
	//return -1;
    
	// m = 32
	if (D) System.out.println("PET: n="+n);
    
	// TODO: Debbie should precompute these, but for now we just do it here
	// pre-computed inputs
	// party D
	BigInteger[] alpha = new BigInteger[n];
	BigInteger[] beta  = new BigInteger[n];
	BigInteger[] tau   = new BigInteger[n];
	BigInteger[] r     = new BigInteger[n];
	BigInteger[] gama  = new BigInteger[n];
	BigInteger[] delta = new BigInteger[n];               
	for (int j=0; j<n; j++) {
	    //alpha[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
	    alpha[j] = Util.nextBigInteger(p);
	}     
	for (int j=0; j<n; j++) {
	    //beta[j]  = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
	    //tau[j]   = BigInteger.valueOf(Math.abs(rnd.nextLong()) % p.longValue()); // [0, p-1], Z_p
	    //r[j]     = BigInteger.valueOf(Math.abs(rnd.nextLong()) % (p.longValue()-1L) + 1L); // [1, p-1], Z_p*
	    beta[j] = Util.nextBigInteger(p);
	    tau[j] = Util.nextBigInteger(p);
	    r[j] = Util.nextBigInteger(p.subtract(BigInteger.ONE)).add(BigInteger.ONE);
	}
	for (int j=0; j<n; j++) {
	    // gama_j <- (alpha_j * beta_j - tau_j) mod p
	    gama[j]  = alpha[j].multiply(beta[j]).subtract(tau[j]).mod(p);
	    // delta_j <- (beta_j + r_j) mod p
	    delta[j] = beta[j].add(r[j]).mod(p);
	}       
	// D sends alpha, gama, delta to C
	charlie.write(alpha);
	charlie.write(gama);
	charlie.write(delta);
    
	// D sends beta, tau, r to E
	eddie.write(beta);
	eddie.write(tau);
	eddie.write(r);
	  
	sanityCheck();
    
    }

    public Integer executeCharlie(Communication debbie, Communication eddie, BigInteger[] cc) {
	debbie.countBandwidth = false;
	eddie.countBandwidth = false;
	  
	// parameters
	int n  = cc.length;
	// m = 32
	//BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    
	if (D) {
	    System.out.println("PET: n="+n);
	    System.out.println("PET: cc="+Arrays.toString(cc));
	}
    
	// TODO: load precomputed values instead of reading here
	BigInteger[] alpha = debbie.readBigIntegerArray();
	BigInteger[] gamma = debbie.readBigIntegerArray();
	BigInteger[] delta = debbie.readBigIntegerArray();
    
	// on-line inputs
	timing.pet_online.start();
	BigInteger[] c = new BigInteger[n];
	for (int j=0; j<n; j++) {
	    c[j] = cc[j];
	}
    
	debbie.countBandwidth = true;
	eddie.countBandwidth = true;
	debbie.bandwidth[PID.pet].start();
	eddie.bandwidth[PID.pet].start();
	
	sanityCheck();
    
	// Protocol       
	// step 1 
	// party C
	BigInteger[] u = new BigInteger[n];
	for (int j=0; j<n; j++) {
	    // u_j <- (alpha_j - c_j) mod p
	    u[j] = alpha[j].subtract(c[j]).mod(p);
	}
	timing.pet_online.stop();
	//System.out.println("pet online: " + timing.pet_online);
	//sanityCheck(eddie);
	// C sends u to E
	timing.pet_write.start();
	eddie.write(u);
	timing.pet_write.stop();
    
	// step 2
	//sanityCheck(eddie);
	// E sends w to C
	timing.pet_read.start();
	BigInteger[] w = eddie.readBigIntegerArray();
	timing.pet_read.stop();
    
	// step 3
	// party Cforest
	timing.pet_online.start();
	BigInteger[] v = new BigInteger[n];
	for (int j=0; j<n; j++) {
	    // v_j <- (c_j * delta_j + w_j - gama_j) mod p
	    v[j] = c[j].multiply(delta[j]).add(w[j]).subtract(gamma[j]).mod(p);
      
	    if (v[j].longValue() == 0L) {
		timing.pet_online.stop();

		debbie.bandwidth[PID.pet].stop();
      		eddie.bandwidth[PID.pet].stop();
		debbie.countBandwidth = false;
		eddie.countBandwidth = false;
		// C outputs j s.t. v[j] = 0
		return j;
	    }
	}
	timing.pet_online.stop();
    
	debbie.bandwidth[PID.pet].stop();
	eddie.bandwidth[PID.pet].stop();
	debbie.countBandwidth = false;
	eddie.countBandwidth = false;
    
	// this means error
	return -1;
    }
  
    public void executeEddie(Communication charlie, Communication debbie, BigInteger[] bb) {
	charlie.countBandwidth = false;
	debbie.countBandwidth = false;
	    
	// parameters
	int n  = bb.length;
	// m = 32
    
	//BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    
	if (D) {
	    System.out.println("PET: n="+n);
	    System.out.println("PET: bb="+Arrays.toString(bb));
	}
    
	// TODO: load precomputed values instead of reading here
	BigInteger[] beta = debbie.readBigIntegerArray();
	BigInteger[] tau = debbie.readBigIntegerArray();
	BigInteger[] r = debbie.readBigIntegerArray();
    
	// on-line inputs
	timing.pet_online.start();
	BigInteger[] b = new BigInteger[n];
	for (int j=0; j<n; j++) {
	    b[j] = bb[j];
	}
	timing.pet_online.stop();
    
	charlie.countBandwidth = true;
	debbie.countBandwidth = true;
	debbie.bandwidth[PID.pet].start();
	charlie.bandwidth[PID.pet].start();
	
	sanityCheck();
    
	// step 1 
	//sanityCheck(charlie);
	// C sends u to E
	timing.pet_read.start();
	BigInteger[] u = charlie.readBigIntegerArray();
	timing.pet_read.stop();
    
	// step 2
	// party E
	timing.pet_online.start();
	BigInteger[] w = new BigInteger[n];
	for (int j=0; j<n; j++) {
	    // w_j <- (beta_j * u_j - r_j * b_j - tau_j) mod p
	    w[j] = beta[j].multiply(u[j]).subtract(r[j].multiply(b[j])).subtract(tau[j]).mod(p);
	}
	timing.pet_online.stop();
    
	//sanityCheck(charlie);
	// E sends w to C
	timing.pet_write.start();
	charlie.write(w);
	timing.pet_write.stop();

	debbie.bandwidth[PID.pet].stop();
	charlie.bandwidth[PID.pet].stop();
	charlie.countBandwidth = false;
	debbie.countBandwidth = false;
    }

    @Override
    public void run(Party party, Forest unused) throws ForestException {
	throw new NotImplementedException("No testing for PET yet");
    }
}
