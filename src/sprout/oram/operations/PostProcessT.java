package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;


// TODO: This operation is unlike the other TreeOperations we may want to 
//   Extend Operation ourselves, or redefine execute & run
public class PostProcessT extends TreeOperation<BigInteger, BigInteger[]>{
	
    public PostProcessT() {
	super(null, null);
    }

    public PostProcessT(Communication con1, Communication con2) {
	super(con1, con2);
    }

    @Override
	public BigInteger executeCharlieSubTree(Communication debbie, Communication eddie, 
						BigInteger Li, Tree u2, BigInteger[] extraArgs) {
	if (extraArgs.length != 5) {
	    throw new IllegalArgumentException("Must supply sC_Ti, sC_Li_p, sC_Lip1_p, Lip1, Nip1_pr to charlie");
	}
    
	BigInteger secretC_Ti = extraArgs[0];
	BigInteger secretC_Li_p = extraArgs[1];
	BigInteger secretC_Lip1_p = extraArgs[2];
	BigInteger Lip1 = extraArgs[3];
	BigInteger Nip1_pr = extraArgs[4];
	int Nip1_pr_int = 0;
	if (i < h)
	    Nip1_pr_int = Nip1_pr.intValue();
    
    
	// protocol
	// i = 0 case
	if (i == 0) {
	    //Li = "";
	    Li = null;
	    secretC_Li_p = null;
	}
    
	// protocol doesn't run for i=h case
	if (i == h) {
	    int d_size = ForestMetadata.getABits(i);
	    // party C
	    timing.post_online.start();
	    BigInteger triangle_C = Li.xor(secretC_Li_p).shiftLeft(d_size);  
	    BigInteger secretC_Ti_p = secretC_Ti.xor(triangle_C);
	    timing.post_online.stop();
	    return secretC_Ti_p;      
	}
    
	debbie.countBandwidth = true;
	eddie.countBandwidth = true;
	debbie.bandwidth[PID.ppt].start();
	eddie.bandwidth[PID.ppt].start();
    
	sanityCheck();
    
	// step 1
	// E sends delta_C to C
	sanityCheck();
	timing.post_read.start();
	BigInteger delta_C = eddie.readBigInteger();
	timing.post_read.stop();
    
	// step 2
	// party C
	timing.post_online.start();
	int alpha = SR.rand.nextInt(twotaupow) + 1;   // [1, 2^tau]
	int j_p = BigInteger.valueOf(Nip1_pr_int+alpha).mod(BigInteger.valueOf(twotaupow)).intValue();
	timing.post_online.stop();
    
	sanityCheck();
	timing.post_write.start();
	// C sends j_p to D
	debbie.write(j_p);
	// C sends alpha to E
	eddie.write(alpha);
	timing.post_write.stop();
    
	// step 3
	// D sends s to C
	sanityCheck();
	timing.post_read.start();
	byte[] s = debbie.read();
	timing.post_read.stop();
    
	// step 4
	// party C
	PRG G = new PRG(aBits);
    
	timing.post_online.start();
	BigInteger[] a = new BigInteger[twotaupow];
	BigInteger a_all = new BigInteger(1, G.compute(s));
	BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(BigInteger.ONE);
	BigInteger tmp = a_all;
	for (int k=twotaupow-1; k>=0; k--) {
	    a[k] = tmp.and(helper);
	    tmp = tmp.shiftRight(d_ip1);
	}
    
	BigInteger[] e = new BigInteger[twotaupow];
	BigInteger A_C = BigInteger.ZERO;
	for (int k=0; k<twotaupow; k++) {
	    e[k] = a[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
	    if (k == Nip1_pr_int)
    		e[k] = e[k].xor(Lip1).xor(secretC_Lip1_p).xor(delta_C);
	    A_C = A_C.shiftLeft(d_ip1).xor(e[k]);
	}
	BigInteger triangle_C;
	if (i == 0)
	    triangle_C = A_C;
	else
	    triangle_C = Li.xor(secretC_Li_p).shiftLeft(aBits).xor(A_C);
	BigInteger secretC_Ti_p = secretC_Ti.xor(triangle_C);
	timing.post_online.stop();
    
	debbie.countBandwidth = false;
	eddie.countBandwidth = false;
	debbie.bandwidth[PID.ppt].stop();
	eddie.bandwidth[PID.ppt].stop();
    
	// C outputs secretC_Ti_p
	return secretC_Ti_p;
    }

    @Override
	public BigInteger executeDebbieSubTree(Communication charlie, Communication eddie, 
					       BigInteger u1, Tree u3, BigInteger[] u4) {
	if (i == h) {
	    return null;    
	}
    
	charlie.countBandwidth = true;
	eddie.countBandwidth = true;	  
	charlie.bandwidth[PID.ppt].start();
	eddie.bandwidth[PID.ppt].start();
	  
	sanityCheck();
    
	// step 1
	// E sends delta_D to D
	sanityCheck();
	timing.post_read.start();
	BigInteger delta_D = eddie.readBigInteger();
	timing.post_read.stop(); 
    
	// step 2
	sanityCheck();
	// C sends j_p to D
	timing.post_read.start();
	int j_p = charlie.readInt();
	timing.post_read.stop();
    
	// step 3
	// party D
	timing.post_online.start();
	byte[] s = SR.rand.generateSeed(16);  // 128 bits
	timing.post_online.stop();
    
	PRG G = new PRG(aBits);
    
	timing.post_online.start();
	BigInteger[] a = new BigInteger[twotaupow];
	BigInteger[] a_p = new BigInteger[twotaupow];
	BigInteger a_all = new BigInteger(1, G.compute(s));
	BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(BigInteger.ONE);
	BigInteger tmp = a_all;
	for (int k=twotaupow-1; k>=0; k--) {
	    a[k] = tmp.and(helper);
	    tmp = tmp.shiftRight(d_ip1);
	    if (k != j_p)
		a_p[k] = a[k];
	    else
		a_p[k] = a[k].xor(delta_D);
	}
	timing.post_online.stop();
    
	sanityCheck();
	timing.post_write.start();
	// D sends s to C
	charlie.write(s);
	// D sends a_p to E
	eddie.write(a_p);
	timing.post_write.stop();
    
	charlie.countBandwidth = false;
	eddie.countBandwidth = false;	  
	charlie.bandwidth[PID.ppt].stop();
	eddie.bandwidth[PID.ppt].stop();
    
	return null;
    }

    @Override
	public BigInteger executeEddieSubTree(Communication charlie, Communication debbie, 
					      Tree u2, BigInteger[] extraArgs) {
	if (extraArgs.length != 3) {
	    throw new IllegalArgumentException("Must supply sE_Ti, sE_Li_p, and sE_Lip1_p to eddie");
	}
    
	BigInteger secretE_Ti = extraArgs[0];
	BigInteger secretE_Li_p = extraArgs[1];
	BigInteger secretE_Lip1_p = extraArgs[2];
    

	// protocol
	// i = 0 case
	if (i == 0) {
	    //secretE_Li_p = "";
	    secretE_Li_p = null;
	}
    
	// protocol doesn't run for i=h case
	if (i == h) {
	    int d_size = ForestMetadata.getABits(i);
	    // party E
	    timing.post_online.start();
	    //String triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + Util.addZero("", d_size);
	    //String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBits);
	    BigInteger triangle_E = secretE_Li_p.shiftLeft(d_size);
	    BigInteger secretE_Ti_p = secretE_Ti.xor(triangle_E);
	    timing.post_online.stop();
	    return secretE_Ti_p;      
	}
    
	charlie.countBandwidth = true;
	debbie.countBandwidth = true;
	charlie.bandwidth[PID.ppt].start();
	debbie.bandwidth[PID.ppt].start();
	  
	sanityCheck();
    
	// step 1
	// party E
	timing.post_online.start();
	BigInteger delta_D = new BigInteger(d_ip1, SR.rand);
	BigInteger delta_C = delta_D.xor(secretE_Lip1_p);
	timing.post_online.stop();
	// E sends delta_C to C and delta_D to D
	sanityCheck();
	timing.post_write.start();
	debbie.write(delta_D);
	charlie.write(delta_C);
	timing.post_write.stop();

	// step 2
	sanityCheck();
	// C sends alpha to E
	timing.post_read.start();
	int alpha = charlie.readInt();
	timing.post_read.stop();
    
	// step 3
	sanityCheck();
	// D sends a_p to E
	timing.post_read.start();
	//byte[][] a_p_byte = debbie.readDoubleByteArray();
	BigInteger[] a_p = debbie.readBigIntegerArray();
	timing.post_read.stop();
    
	// step 5
	// party E
	timing.post_online.start();
	//String[] a_p = new String[twotaupow];
	//for (int k=0; k<twotaupow; k++)
	//	a_p[k] = Util.addZero(a_p_byte[k].toString(2), d_ip1);
    
	BigInteger A_E = BigInteger.ZERO;
	for (int k=0; k<twotaupow; k++) {
	    //A_E += a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
	    A_E = A_E.shiftLeft(d_ip1).xor(a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()]);
	}
	BigInteger triangle_E;
	if (i == 0)
	    triangle_E = A_E;
	else
	    //triangle_E = "0" + Util.addZero("", i*tau) + Util.addZero(secretE_Li_p.toString(2), lBits) + A_E;
	    triangle_E = secretE_Li_p.shiftLeft(aBits).xor(A_E);
	BigInteger secretE_Ti_p = secretE_Ti.xor(triangle_E);
	timing.post_online.stop();
    
	charlie.bandwidth[PID.ppt].stop();
	debbie.bandwidth[PID.ppt].stop();
	charlie.countBandwidth = false;
	debbie.countBandwidth = false;
    
	// E outputs secretE_Ti_p
	return secretE_Ti_p;
    }
  
    /*
      @Override
      public String [] prepareArgs(Party party) {
      String Lip1       = Util.addZero(new BigInteger(d_ip1, SR.rand).toString(2), d_ip1);
      String Nip1_pr      = Util.addZero(new BigInteger(tau, SR.rand).toString(2), tau);
      String secret_Ti     = Util.addZero(new BigInteger(tupleBits, SR.rand).toString(2), tupleBits);
      String secret_Li_p   = Util.addZero(new BigInteger(d_i, SR.rand).toString(2), d_i);
      String secret_Lip1_p = Util.addZero(new BigInteger(d_ip1, SR.rand).toString(2), d_ip1);
    
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
    */
}
