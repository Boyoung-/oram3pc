package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.Tree;
import sprout.util.Util;

// TODO: remove testing code
public class Access extends TreeOperation<AOutput, String> {
  
  public Access(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  @Override
  public AOutput executeCharlieSubTree(Communication debbie, Communication eddie,
                                       String Li, Tree unused, String Nip1) {
	  
    // prepare                                 
    String Ni = Nip1.substring(0, nBits);     
    String Nip1_pr = Nip1.substring(nBits);  
    
    debbie.countBandwidth = true;
    eddie.countBandwidth = true;
    debbie.bandwidth[PID.access].start();
    eddie.bandwidth[PID.access].start();
    
    sanityCheck();
    
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
    DecryptPath dp = new DecryptPath(debbie, eddie);
    dp.loadTreeSpecificParameters(i);
    timing.decrypt.start();
    DPOutput DecOut = dp.executeCharlieSubTree(debbie, eddie, Li, null, null); 
    timing.decrypt.stop();
    BigInteger secretC_P = DecOut.secretC_P[0];
    for (int j=1; j<DecOut.secretC_P.length; j++)
    	secretC_P = secretC_P.shiftLeft(bucketBits).xor(DecOut.secretC_P[j]);
    
    
    // step 3
    // party C and E
    int j_1 = 0; // i = 0 case; as the j_1 = 1 in the write up
    String[] a = new String[pathTuples];
    String[] c = new String[pathTuples];
    if (i > 0) {
    	timing.access_online.start();
      for (int j=0; j<pathTuples; j++) {
        a[j] = Util.addZero(secretC_P.toString(2), pathTuples*tupleBits).substring(j*tupleBits, j*tupleBits+1+nBits); // party C
        c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger("1"+Ni, 2)).toString(2), 1+nBits); // party C
      }
      timing.access_online.stop();
      //sanityCheck()();
      PET pet = new PET(debbie, eddie);
      timing.pet.start();
      j_1 = pet.executeCharlie(debbie, eddie, c);
      timing.pet.stop();
      // PET outputs j_1 for C
    }
    if (j_1 < 0) {
    	try {
			throw new Exception("PET error!");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // step 4
    AOT aot = new AOT(debbie, eddie);
    String fbar = Util.addZero("", aBits); // i = 0 case
    if (i > 0) {
      //sanityCheck();
      timing.aot.start();
      fbar = aot.executeC(debbie, eddie, j_1);
      timing.aot.stop();
      // outputs fbar for C
    }
    
    // step 5
    int j_2 = 0;
    String ybar_j2 = "";  // i = h case
    if (i < h) {
      // AOT(E, C, D)
      //sanityCheck();
      j_2 = new BigInteger(Nip1_pr, 2).intValue();
      timing.aot.start();
      ybar_j2 = aot.executeC(debbie, eddie, j_2);
      timing.aot.stop();
      // outputs ybar_j2 for C
    }
    
    // step 6
    // party C
    String ybar = "";
    String zeros = Util.addZero("", d_ip1);
    timing.access_online.start();
    for (int o=0; o<twotaupow; o++) {
      if (i < h && o == j_2)
        ybar += ybar_j2;
      else // i = h case
        ybar += zeros;
    }
    
    String secretC_Aj1;
    if (i == 0)
      secretC_Aj1 = Util.addZero(secretC_P.toString(2), pathTuples*tupleBits);
    else
      secretC_Aj1 = Util.addZero(secretC_P.toString(2), pathTuples*tupleBits).substring(j_1*tupleBits+1+nBits+lBits, (j_1+1)*tupleBits);
    String Abar = Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).xor(new BigInteger(ybar, 2)).toString(2), aBits);
    
    String d = "";
    String Lip1 = ""; // i = h case
    if (i < h) {
      Lip1 = Abar.substring(j_2*d_ip1, (j_2+1)*d_ip1);
    }
    else {
      d = Abar;
    }
    
    String secretC_Ti = "1" + Ni + Li + Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2), aBits);
    if (i == 0)
      secretC_Ti = Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2), aBits);
    String secretC_P_p = ""; // i = 0 case
    if (i > 0) {
      int flipBit = 1 - Integer.parseInt(Util.addZero(secretC_P.toString(2), pathTuples*tupleBits).substring(j_1*tupleBits, j_1*tupleBits+1));
      String newTuple = flipBit + Util.addZero(new BigInteger(tupleBits-1, SR.rand).toString(2), tupleBits-1);
      secretC_P_p = Util.addZero(secretC_P.toString(2), pathTuples*tupleBits).substring(0, j_1*tupleBits) + newTuple + Util.addZero(secretC_P.toString(2), pathTuples*tupleBits).substring((j_1+1)*tupleBits);
    }
    timing.access_online.stop();
    
    debbie.bandwidth[PID.access].stop();
    eddie.bandwidth[PID.access].stop();
    debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    
    //sanityCheck();
    // C outputs Lip1, secretC_Ti, secretC_P_p
    return new AOutput(Lip1, null, secretC_Ti, null, secretC_P_p, null, d);
    
    //return null;
  }
  
  @Override
  public AOutput executeDebbieSubTree(Communication charlie, Communication eddie,
                                      BigInteger k, Tree unused1, String unused2) {
	  charlie.countBandwidth = true;
	  eddie.countBandwidth = true;	  
	  charlie.bandwidth[PID.access].start();
	  eddie.bandwidth[PID.access].start();
	  
	  sanityCheck();
	  
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
	DecryptPath dp = new DecryptPath(charlie, eddie);
	dp.loadTreeSpecificParameters(i);
    timing.decrypt.start();
    dp.executeDebbieSubTree(charlie, eddie, k, null, null);
    timing.decrypt.stop();
    // DecryptPath outpus sigma and secretE_P for E and secretC_P for C
    

    AOT aot = new AOT(charlie, eddie);
    if (i > 0) {
    	// step 3
      //sanityCheck();
      PET pet = new PET(charlie, eddie);
      timing.pet.start();
      pet.executeDebbie(charlie, eddie, pathTuples);
      timing.pet.stop();
      // PET outputs j_1 for C
      
      //sanityCheck();
      // step 4
      timing.aot.start();
      aot.executeD(charlie, eddie);
      timing.aot.stop();
    }
    
    // step 5
    if (i < h) {
      // AOT(E, C, D)
      //sanityCheck();
      timing.aot.start();
      aot.executeD(charlie, eddie); 
      timing.aot.stop();
      // outputs ybar_j2 for C
    }
    
    
	  charlie.bandwidth[PID.access].stop();
	  eddie.bandwidth[PID.access].stop();
    charlie.countBandwidth = false;
    eddie.countBandwidth = false;
    
    //sanityCheck();
    //return new AOutput(); 
    return null;
  }
  
  @Override
  public AOutput executeEddieSubTree(Communication charlie, Communication debbie,
                                     Tree OT, String unused) {
	  charlie.countBandwidth = true;
	  debbie.countBandwidth = true;
	  charlie.bandwidth[PID.access].start();
	  debbie.bandwidth[PID.access].start();
	  
	  sanityCheck();
	  
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
	DecryptPath dp = new DecryptPath(charlie, debbie);
	dp.loadTreeSpecificParameters(i);
    timing.decrypt.start();
    DPOutput DecOut = dp.executeEddieSubTree(charlie, debbie, OT, null); 
    timing.decrypt.stop();
    BigInteger secretE_P = DecOut.secretE_P[0];
    for (int j=1; j<DecOut.secretE_P.length; j++)
    	secretE_P = secretE_P.shiftLeft(bucketBits).xor(DecOut.secretE_P[j]);
    // DecryptPath outpus sigma and secretE_P for E and secretC_P for C
    
   
    // step 2
    // party E
    timing.access_online.start();
    //String[] y = new String[twotaupow];
    //String y_all;
    BigInteger[] y = new BigInteger[twotaupow];
    BigInteger y_all;
    if (i == 0) 
      //y_all = Util.addZero(secretE_P.toString(2), tupleBits*pathTuples);
    	y_all = secretE_P;
    else if (i < h)
      //y_all = Util.addZero(new BigInteger(aBits, SR.rand).toString(2), aBits);
    	y_all = new BigInteger(aBits, SR.rand);
    else // i = h
      //y_all = Util.addZero("", aBits);
    	y_all = BigInteger.ZERO;
    //for (int o=0; o<twotaupow; o++) {
      //y[o] = y_all.substring(o*d_ip1, (o+1)*d_ip1);
    //}
    BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(BigInteger.ONE);
    BigInteger tmp = y_all;
    for (int o=twotaupow-1; o>=0; o--) {
    	y[o] = tmp.and(helper);
    	tmp = tmp.shiftRight(d_ip1);
    }
    
    //String secretE_Ti = "0" + Util.addZero("", i*tau) + Util.addZero ("", d_i) + y_all;
    //if (i == 0)
      //secretE_Ti = y_all;
    BigInteger secretE_Ti = y_all;
  //String secretE_P_p = ""; //  i = 0 case
    //if (i > 0) { 
    //  secretE_P_p = secretE_P;
    //}
    BigInteger secretE_P_p = null;
    if (i > 0)
    	secretE_P_p = secretE_P;
    timing.access_online.stop();
    
    // step 3
    // party C and E
    String[] b = new String[pathTuples];
    if (i > 0) {
    	timing.access_online.start();
      for (int j=0; j<pathTuples; j++) {
        b[j] = Util.addZero(secretE_P.toString(2), tupleBits*pathTuples).substring(j*tupleBits, j*tupleBits+1+nBits); // party E
      }
      timing.access_online.stop();
      //sanityCheck();
      PET pet = new PET(charlie, debbie);
      timing.pet.start();
      pet.executeEddie(charlie, debbie, b);
      timing.pet.stop();
      // PET outputs j_1 for C
    }
    
    AOT aot = new AOT(charlie, debbie);
    // step 4
    // party E
    if (i > 0) {
        timing.access_online.start();
      String[] e = new String[pathTuples];
      //String[] f = new String[pathTuples];
      BigInteger[] f = new BigInteger[pathTuples];
      for (int o=0; o<pathTuples; o++) {
        e[o] = Util.addZero(secretE_P.toString(2), tupleBits*pathTuples).substring(o*tupleBits+1+nBits+lBits, (o+1)*tupleBits);
        //f[o] = Util.addZero(new BigInteger(e[o], 2).xor(y_all).toString(2), aBits);
        f[o] = new BigInteger(e[o], 2).xor(y_all);
      }
      timing.access_online.stop();
      
      //sanityCheck();
      // AOT(E, C, D)
      timing.aot.start();
      aot.executeE(charlie, debbie, f, aBits);
      timing.aot.stop();
      // outputs fbar for C
    }
    
    // step 5
    if (i < h) {
      // AOT(E, C, D)
      //sanityCheck();
      timing.aot.start();
      aot.executeE(charlie, debbie, y, d_ip1);
      timing.aot.stop();
      // outputs ybar_j2 for C
    }
    

	  charlie.bandwidth[PID.access].stop();
	  debbie.bandwidth[PID.access].stop();
    charlie.countBandwidth = false;
    debbie.countBandwidth = false;
    
    //sanityCheck();
    // E outputs secretE_Ti and secretE_P_p
    if (secretE_P_p == null)
    	return new AOutput(null, DecOut.p, null, Util.addZero(secretE_Ti.toString(2), tupleBits), null, "", null);
    return new AOutput(null, DecOut.p, null, Util.addZero(secretE_Ti.toString(2), tupleBits), null, Util.addZero(secretE_P_p.toString(2), tupleBits*pathTuples), null);
  }

  @Override
  public String prepareArgs() {
    // Nip1 
    // Note: Originally i=0 case has just tau. This should be fine since
    // nBits = i*tau, thus when i=0 nBits = 0 and nBits+tau = tau
    return  Util.addZero(new BigInteger(nBits+tau, SR.rand).toString(2), nBits+tau); 
  }
}
