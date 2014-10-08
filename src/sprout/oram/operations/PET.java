package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;

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
  
  private static boolean D = true;
  
  public PET(Communication con1, Communication con2) {
    super(con1, con2);
  }

  public static Integer executeDebbie(Communication charlie, Communication eddie, int n) {
    // Debbie does nothing online
    //return -1;
    
    // m = 32
    if (D) System.out.println("PET: n="+n);
    
    // TODO: Debbie should precompute these, but for now we just do it here
    // pre-computed inputs
    // party D
    BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
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
    charlie.write(alpha);
    charlie.write(gama);
    charlie.write(delta);
    
    // D sends beta, tau, r to E
    eddie.write(beta);
    eddie.write(tau);
    eddie.write(r);
    
    return -1;
  }

  public static Integer executeCharlie(Communication debbie, Communication eddie, String[] cc) {
    // parameters
    int n  = cc.length;
    // m = 32
    BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    
    if (D) System.out.println("PET: n="+n);
    
    // TODO: load precomputed values instead of reading here
    BigInteger[] alpha = debbie.readBigIntegerArray();
    BigInteger[] gamma = debbie.readBigIntegerArray();
    BigInteger[] delta = debbie.readBigIntegerArray();
    
    // on-line inputs
    BigInteger[] c = new BigInteger[n];
    for (int j=0; j<n; j++) {
      c[j] = new BigInteger(cc[j], 2);
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
    eddie.write(u);
    
    // step 2
    // E sends w to C
    BigInteger[] w = eddie.readBigIntegerArray();
    
    // step 3
    // party Cforest
    BigInteger[] v = new BigInteger[n];
    for (int j=0; j<n; j++) {
      // v_j <- (c_j * delta_j + w_j - gama_j) mod p
      v[j] = c[j].multiply(delta[j]).add(w[j]).subtract(gamma[j]).mod(p);
      
      if (v[j].longValue() == 0L) {
        // C outputs j s.t. v[j] = 0
        return j;
      }
    }
    return -1;
  }
  
  public static Integer executeEddie(Communication charlie, Communication debbie, String[] bb) {
    // parameters
    int n  = bb.length;
    // m = 32
    
    BigInteger p = BigInteger.valueOf((long) Math.pow(2, 34) - 41L); // p = 2^34 - 41
    
    if (D) System.out.println("PET: n="+n);
    
    // TODO: load precomputed values instead of reading here
    BigInteger[] beta = debbie.readBigIntegerArray();
    BigInteger[] tau = debbie.readBigIntegerArray();
    BigInteger[] r = debbie.readBigIntegerArray();
    
    // on-line inputs
    BigInteger[] b = new BigInteger[n];
    for (int j=0; j<n; j++) {
      b[j] = new BigInteger(bb[j], 2);
    }
    
    // step 1 
    // C sends u to E
    BigInteger[] u = charlie.readBigIntegerArray();
    
    // step 2
    // party E
    BigInteger[] w = new BigInteger[n];
    for (int j=0; j<n; j++) {
      // w_j <- (beta_j * u_j - r_j * b_j - tau_j) mod p
      w[j] = beta[j].multiply(u[j]).subtract(r[j].multiply(b[j])).subtract(tau[j]).mod(p);
    }
    // E sends w to C
    charlie.write(w);
    
    return -1;
  }

  @Override
  public void run(Party party, Forest unused) throws ForestException {
    throw new NotImplementedException("No testing for PET yet");
  }
}
