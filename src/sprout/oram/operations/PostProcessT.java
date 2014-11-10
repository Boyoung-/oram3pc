package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.ForestMetadata;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;


// TODO: This operation is unlike the other TreeOperations we may want to 
//   Extend Operation ourselves, or redefine execute & run
public class PostProcessT extends TreeOperation<String, String[]>{
	
	public PostProcessT() {
		super(null, null);
	}

  public PostProcessT(Communication con1, Communication con2) {
    super(con1, con2);
  }

  @Override
  public String executeCharlieSubTree(Communication debbie, Communication eddie, 
		  String Li, Tree u2, String[] extraArgs) {
    if (extraArgs.length != 5) {
      throw new IllegalArgumentException("Must supply sC_Ti, sC_Li_p, sC_Lip1_p, Lip1, Nip1_pr to charlie");
    }
    
    String secretC_Ti = extraArgs[0];
    String secretC_Li_p = extraArgs[1];
    String secretC_Lip1_p = extraArgs[2];
    String Lip1 = extraArgs[3];
    String Nip1_pr = extraArgs[4];
    int Nip1_pr_int = 0;
    if (i < h)
    	Nip1_pr_int = new BigInteger(Nip1_pr, 2).intValue();
    
    /*
    ////////////////////////below are for checking correctness /////////////////////
    String T_i_fb     = "1";
    String T_i_N      = Util.addZero(new BigInteger(nBits, rnd).toString(2), nBits);
    String T_i_L      = Li;
    String T_i_A      = Util.addZero(new BigInteger(aBits, rnd).toString(2), aBits);
    T_i_A         = T_i_A.substring(0, Nip1_pr_int*d_ip1) + Lip1 + T_i_A.substring((Nip1_pr_int+1)*d_ip1);
    String T_i;
    if (i == 0)
      T_i         = T_i_A;
    else
      T_i         = T_i_fb + T_i_N + T_i_L + T_i_A;
    String secretE_Ti    = Util.addZero(new BigInteger(T_i, 2).xor(new BigInteger(secretC_Ti, 2)).toString(2), tupleBits);  
    eddie.write(secretE_Ti);
    //////////////////////// above are for checking correctness /////////////////////
     */
    
    // protocol
    // i = 0 case
    if (i == 0) {
      Li = "";
      secretC_Li_p = "";
    }
    
    // protocol doesn't run for i=h case
    if (i == h) {
      int d_size = ForestMetadata.getABits(i);
      // party C
      timing.post_online.start();
      String triangle_C = "0" + Util.addZero("", i*tau) + Util.addZero(new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2), lBits) + Util.addZero("", d_size);  
      String secretC_Ti_p = Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2), tupleBits);
      timing.post_online.stop();
      return secretC_Ti_p;      
    }
    
    // step 1
    // E sends delta_C to C
    timing.post_read.start();
    byte[] delta_C = eddie.read();
    timing.post_read.stop();
    
    // step 2
    // party C
    timing.post_online.start();
    int alpha = rnd.nextInt(twotaupow) + 1;   // [1, 2^tau]
    int j_p = BigInteger.valueOf(Nip1_pr_int+alpha).mod(BigInteger.valueOf(twotaupow)).intValue();
    timing.post_online.stop();
    
    timing.post_write.start();
    // C sends j_p to D
    debbie.write(j_p);
    // C sends alpha to E
    eddie.write(alpha);
    timing.post_write.stop();
    
    // step 3
    // D sends s to C
    timing.post_read.start();
    byte[] s = debbie.read();
    timing.post_read.stop();
    
    // step 4
    // party C
    PRG G;
    try {
      G = new PRG(aBits);
    } catch (NoSuchAlgorithmException e1) {
      e1.printStackTrace();
      return null;
    }
    timing.post_online.start();
    String[] a = new String[twotaupow];
    String a_all = G.generateBitString(aBits, s);
    for (int k=0; k<twotaupow; k++) {
      a[k] = a_all.substring(k*d_ip1, (k+1)*d_ip1);
    }
    
    String[] e = new String[twotaupow];
    String A_C = ""; 
    for (int k=0; k<twotaupow; k++) {
      e[k] = a[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
      if (k == Nip1_pr_int)
        e[k] = Util.addZero(new BigInteger(e[k], 2).xor(new BigInteger(Lip1, 2)).xor(new BigInteger(secretC_Lip1_p, 2)).xor(new BigInteger(1, delta_C)).toString(2), d_ip1);
      A_C += e[k];
    }
    String triangle_C;
    if (i == 0)
      triangle_C = A_C;
    else
      triangle_C = "0" + Util.addZero("", i*tau) + Util.addZero(new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2), lBits) + A_C;
    String secretC_Ti_p = Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2), tupleBits);
    timing.post_online.stop();
    // C outputs secretC_Ti_p
    
    return secretC_Ti_p;
  }

  @Override
  public String executeDebbieSubTree(Communication charlie, Communication eddie, 
		  BigInteger u1, Tree u3, String[] u4) {
    if (i == h) {
      return null;    
    }
    
    // step 1
    // E sends delta_D to D
    timing.post_read.start();
    byte[] delta_D = eddie.read();
    timing.post_read.stop(); 
    
    // step 2
    // C sends j_p to D
    timing.post_read.start();
    int j_p = charlie.readInt();
    timing.post_read.stop();
    
    // step 3
    // party D
    timing.post_online.start();
    byte[] s = rnd.generateSeed(16);  // 128 bits
    timing.post_online.stop();
    PRG G;
    try {
      G = new PRG(aBits);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    timing.post_online.start();
    String[] a = new String[twotaupow];
    byte[][] a_p = new byte[twotaupow][];
    String a_all = G.generateBitString(aBits, s);
    for (int k=0; k<twotaupow; k++) {
      a[k] = a_all.substring(k*d_ip1, (k+1)*d_ip1);
      if (k != j_p)
        a_p[k] = new BigInteger(a[k], 2).toByteArray();
      else
        a_p[k] = new BigInteger(a[k], 2).xor(new BigInteger(1, delta_D)).toByteArray();
    }
    timing.post_online.stop();
    
    timing.post_write.start();
    // D sends s to C
    charlie.write(s);
    // D sends a_p to E
    eddie.write(a_p);
    timing.post_write.stop();
    
    return null;
  }

  @Override
  public String executeEddieSubTree(Communication charlie, Communication debbie, 
		  Tree u2, String[] extraArgs) {
    if (extraArgs.length != 3) {
      throw new IllegalArgumentException("Must supply sE_Ti, sE_Li_p, and sE_Lip1_p to eddie");
    }
    
    String secretE_Ti = extraArgs[0];
    String secretE_Li_p = extraArgs[1];
    String secretE_Lip1_p = extraArgs[2];
    
    /*
    ////////////////////////below are for checking correctness /////////////////////
    secretE_Ti = charlie.readString();
    //////////////////////// above are for checking correctness /////////////////////
     */

    // protocol
    // i = 0 case
    if (i == 0) {
      secretE_Li_p = "";
    }
    
    // protocol doesn't run for i=h case
    if (i == h) {
      int d_size = ForestMetadata.getABits(i);
      // party E
      timing.post_online.start();
      String triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + Util.addZero("", d_size);
      String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBits);
      timing.post_online.stop();
      return secretE_Ti_p;      
    }
    
    // step 1
    // party E
    timing.post_online.start();
    byte[] delta_D = new BigInteger(d_ip1, rnd).toByteArray();
    byte[] delta_C = new BigInteger(1, delta_D).xor(new BigInteger(secretE_Lip1_p, 2)).toByteArray();
    timing.post_online.stop();
    // E sends delta_C to C and delta_D to D
    timing.post_write.start();
    debbie.write(delta_D);
    charlie.write(delta_C);
    timing.post_write.stop();

    // step 2
    // C sends alpha to E
    timing.post_read.start();
    int alpha = charlie.readInt();
    timing.post_read.stop();
    
    // step 3
    // D sends a_p to E
    timing.post_read.start();
    byte[][] a_p_byte = debbie.readDoubleByteArray();
    timing.post_read.stop();
    
    // step 5
    // party E
    timing.post_online.start();
    String[] a_p = new String[twotaupow];
    for (int k=0; k<twotaupow; k++)
    	a_p[k] = Util.addZero(new BigInteger(1, a_p_byte[k]).toString(2), d_ip1);
    
    String A_E = "";
    for (int k=0; k<twotaupow; k++) {
      A_E += a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
    }
    String triangle_E;
    if (i == 0)
      triangle_E = A_E;
    else
      triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + A_E;
    String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBits);
    timing.post_online.stop();
    // E outputs secretE_Ti_p
    
    return secretE_Ti_p;
  }
  
  @Override
  public String [] prepareArgs(Party party) {
    String Lip1       = Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
    String Nip1_pr      = Util.addZero(new BigInteger(tau, rnd).toString(2), tau);
    String secret_Ti     = Util.addZero(new BigInteger(tupleBits, rnd).toString(2), tupleBits);
    String secret_Li_p   = Util.addZero(new BigInteger(d_i, rnd).toString(2), d_i);
    String secret_Lip1_p = Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
    
    switch (party) {
    case Charlie:
      return new String[]{secret_Ti, secret_Li_p, secret_Lip1_p, Lip1, Nip1_pr};
    case Debbie:
      return null;
    case Eddie:
      return new String[]{secret_Ti, secret_Li_p, secret_Lip1_p};
    }
    
    return null;
  }

}
