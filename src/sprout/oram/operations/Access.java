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
    BigInteger[] a = new BigInteger[pathTuples];
    BigInteger[] c = new BigInteger[pathTuples];
    if (i > 0) {
    	timing.access_online.start();
      for (int j=0; j<pathTuples; j++) {
    	  a[j] = Util.getSubBits(secretC_P, (pathTuples-j)*tupleBits-1-nBits, (pathTuples-j)*tupleBits); // TODO: better way?
    	  c[j] = new BigInteger(Ni, 2).setBit(nBits).xor(a[j]);
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
    BigInteger fbar;
    if (i == 0)
    	fbar = BigInteger.ZERO;
    else {
      //sanityCheck();
      timing.aot.start();
      fbar = aot.executeC(debbie, eddie, j_1);
      timing.aot.stop();
      // outputs fbar for C
    }
    
    // step 5
    int j_2 = 0;
    //String ybar_j2 = "";  // i = h case
    BigInteger ybar_j2 = null;
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
    BigInteger ybar = BigInteger.ZERO;
    timing.access_online.start();
    for (int o=0; o<twotaupow; o++) {
    	ybar = ybar.shiftLeft(d_ip1);
      if (i < h && o == j_2)
    	  ybar = ybar.xor(ybar_j2);
    }
    
    BigInteger secretC_Aj1;
    if (i == 0)
      secretC_Aj1 = secretC_P;
    else
    	secretC_Aj1 = Util.getSubBits(secretC_P, (pathTuples-j_1-1)*tupleBits, (pathTuples-j_1-1)*tupleBits+aBits);
    BigInteger Abar = secretC_Aj1.xor(fbar).xor(ybar);
    
    BigInteger d = null;
    BigInteger Lip1 = null;
    if (i < h) {
    	Lip1 = Util.getSubBits(Abar, (twotaupow-j_2-1)*d_ip1, (twotaupow-j_2)*d_ip1);
    }
    else {
      d = Abar;
    }
    
    BigInteger secretC_Ti = secretC_Aj1.xor(fbar);
    if (i > 0)
    	secretC_Ti = new BigInteger(Ni, 2).shiftLeft(lBits+aBits).xor(new BigInteger(Li, 2).shiftLeft(aBits)).xor(secretC_Ti).setBit(tupleBits-1);
    BigInteger secretC_P_p = null;
    if (i > 0) {
    	boolean flipBit = !secretC_P.testBit((pathTuples-j_1)*tupleBits-1);
    	BigInteger newTuple = new BigInteger(tupleBits-1, SR.rand);
    	if (flipBit)
    		newTuple = newTuple.setBit(tupleBits-1);
    	BigInteger tmp1 = Util.getSubBits(secretC_P, (pathTuples-j_1)*tupleBits, pathTuples*tupleBits);
    	BigInteger tmp2 = Util.getSubBits(secretC_P, 0, (pathTuples-j_1-1)*tupleBits);
    	secretC_P_p = tmp1.shiftLeft((pathTuples-j_1)*tupleBits).xor(newTuple.shiftLeft((pathTuples-j_1-1)*tupleBits)).xor(tmp2); //TODO:better way?
    }
    timing.access_online.stop();
    
    debbie.bandwidth[PID.access].stop();
    eddie.bandwidth[PID.access].stop();
    debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    
  //sanityCheck();
    // C outputs Lip1, secretC_Ti, secretC_P_p    
    return new AOutput(Lip1, null, secretC_Ti, null, secretC_P_p, null, d);
    
    /*
    String out_Lip1, out_secretC_P_p, out_d;
    if (Lip1 == null)
    	out_Lip1 = "";
    else
    	out_Lip1 = Util.addZero(Lip1.toString(2), d_ip1);
    
    if (secretC_P_p == null)
    	out_secretC_P_p = "";
    else
    	out_secretC_P_p = Util.addZero(secretC_P_p.toString(2), pathTuples*tupleBits);
    if (d == null)
    	out_d = "";
    else
    	out_d = Util.addZero(d.toString(2), twotaupow*d_ip1);
    return new AOutput(out_Lip1, null, Util.addZero(secretC_Ti.toString(2), tupleBits), null, out_secretC_P_p, null, out_d);
    */
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
    BigInteger[] y = new BigInteger[twotaupow];
    BigInteger y_all;
    if (i == 0) 
    	y_all = secretE_P;
    else if (i < h)
    	y_all = new BigInteger(aBits, SR.rand);
    else // i = h
    	y_all = BigInteger.ZERO;
    BigInteger helper = BigInteger.ONE.shiftLeft(d_ip1).subtract(BigInteger.ONE);
    BigInteger tmp = y_all;
    for (int o=twotaupow-1; o>=0; o--) {
    	y[o] = tmp.and(helper);
    	tmp = tmp.shiftRight(d_ip1);
    }
    
    BigInteger secretE_Ti = y_all;
    BigInteger secretE_P_p = null;
    if (i > 0)
    	secretE_P_p = secretE_P;
    timing.access_online.stop();
    
    
    // step 3
    // party C and E
    BigInteger[] b = new BigInteger[pathTuples];
    if (i > 0) {
    	timing.access_online.start();
      for (int j=0; j<pathTuples; j++) {
    	  b[j] = Util.getSubBits(secretE_P, (pathTuples-j)*tupleBits-1-nBits, (pathTuples-j)*tupleBits); //TODO: better way?
      }
      timing.access_online.stop();
      //sanityCheck();
      PET pet = new PET(charlie, debbie);
      timing.pet.start();
      pet.executeEddie(charlie, debbie, b);
      timing.pet.stop();
      // PET outputs j_1 for C
    }
    
    // step 4
    // party E
    AOT aot = new AOT(charlie, debbie);
   if (i > 0) {
        timing.access_online.start();
      BigInteger[] e = new BigInteger[pathTuples];
      BigInteger[] f = new BigInteger[pathTuples];
      for (int o=0; o<pathTuples; o++) {
        e[o] = Util.getSubBits(secretE_P, (pathTuples-o-1)*tupleBits, (pathTuples-o-1)*tupleBits+aBits); //TODO: better way?
        f[o] = e[o].xor(y_all);
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
    return new AOutput(null, DecOut.p, null, secretE_Ti, null, secretE_P_p, null);
    
    /*
    if (secretE_P_p == null)
    	return new AOutput(null, DecOut.p, null, Util.addZero(secretE_Ti.toString(2), tupleBits), null, "", null);
    return new AOutput(null, DecOut.p, null, Util.addZero(secretE_Ti.toString(2), tupleBits), null, Util.addZero(secretE_P_p.toString(2), tupleBits*pathTuples), null);
  */
  }

  @Override
  public String prepareArgs() {
    // Nip1 
    // Note: Originally i=0 case has just tau. This should be fine since
    // nBits = i*tau, thus when i=0 nBits = 0 and nBits+tau = tau
    return  Util.addZero(new BigInteger(nBits+tau, SR.rand).toString(2), nBits+tau); 
  }
}
