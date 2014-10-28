package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.oram.Tree;
import sprout.util.Util;

// TODO: remove testing code
public class Access extends TreeOperation<AOutput, String> {
  
  public Access(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  /*
  public Access(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }
  */
  
  @Override
  public AOutput executeCharlieSubTree(Communication debbie, Communication eddie,
                                       String Li, Tree OT, String Nip1) {
    // prepare                                 
    String Ni = Nip1.substring(0, nBits);                         
    String Nip1_pr = Nip1.substring(nBits);
    
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
    DPOutput DecOut = (new DecryptPath()).executeCharlieSubTree(debbie, eddie, Li, OT, null);   
    String secretC_P = "";
    for (int j=0; j<DecOut.secretC_P.length; j++)
    	secretC_P += DecOut.secretC_P[j];
    
    ////////////////////////below are for checking correctness /////////////////////
    //System.out.println("-----checking correctness-----");
    eddie.write(Ni);
    eddie.write(Li);
    secretC_P = Util.addZero(new BigInteger(tupleBits*pathTuples, rnd).toString(2), tupleBits*pathTuples);
    eddie.write(secretC_P);
    //System.out.println("-----done with correctness----");
    //////////////////////// above are for checking correctness /////////////////////

    // step 3
    // party C and E
    int j_1 = 0; // i = 0 case; as the j_1 = 1 in the write up
    String[] a = new String[pathTuples];
    String[] c = new String[pathTuples];
    if (i > 0) {
      for (int j=0; j<pathTuples; j++) {
        a[j] = secretC_P.substring(j*tupleBits, j*tupleBits+1+nBits); // party C
        c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger("1"+Ni, 2)).toString(2), 1+nBits); // party C
      }
      sanityCheck();
      j_1 = PET.executeCharlie(debbie, eddie, c);
      // PET outputs j_1 for C
    }
    if (j_1 < 0) {
      System.out.println("PET error!");
      return new AOutput();
    }
    
    // step 4
    String fbar = Util.addZero("", aBits); // i = 0 case
    if (i > 0) {
      sanityCheck();
      fbar = AOT.executeC(debbie, eddie, j_1);
      // outputs fbar for C
    }
    
    // step 5
    int j_2 = new BigInteger(Nip1_pr, 2).intValue();
    String ybar_j2 = "";  // i = h case
    if (i < h) {
      // AOT(E, C, D)
      sanityCheck();
      ybar_j2 = AOT.executeC(debbie, eddie, j_2);
      // outputs ybar_j2 for C
    }
    
    // step 6
    // party C
    String ybar = "";
    String zeros = Util.addZero("", d_ip1);
    for (int o=0; o<twotaupow; o++) {
      if (o == j_2 && i < h)
        ybar += ybar_j2;
      else // i = h case
        ybar += zeros;
    }
    String secretC_Aj1;
    if (i == 0)
      secretC_Aj1 = secretC_P;
    else
      secretC_Aj1 = secretC_P.substring(j_1*tupleBits+1+nBits+lBits, (j_1+1)*tupleBits);
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
      int flipBit = 1 - Integer.parseInt(secretC_P.substring(j_1*tupleBits, j_1*tupleBits+1));
      String newTuple = flipBit + Util.addZero(new BigInteger(tupleBits-1, rnd).toString(2), tupleBits-1);
      secretC_P_p = secretC_P.substring(0, j_1*tupleBits) + newTuple + secretC_P.substring((j_1+1)*tupleBits);
    }
    
    //////////////////////// below are for checking correctness /////////////////////
    System.out.println("---Correctness Test Results---");
    
    String secretE_Ti = eddie.readString();
    String T_i = eddie.readString();
    int test_j1 = eddie.readInt();
    System.out.println("j1: " + test_j1 + " =? " + j_1);
    if (i > 0) {
      String [] b = eddie.readStringArray();
    
      for (int o=0; o<pathTuples; o++)
        if (b[o].equals(c[o]))
          System.out.println("  " + o + ":\tmatch");
    }
    if (Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(secretE_Ti, 2)).toString(2), tupleBits)
                                                                                                        .equals(T_i)){
      System.out.println("Ti: true");
    } else {
      System.out.println("Ti: false");
      
      if (test_j1 == j_1) {
        System.out.println("Ti: " + T_i);
        System.out.println("secretC_Ti: " + secretC_Ti);
        System.out.println("secretE_Ti: " + secretE_Ti);
        System.out.println("computed: " + Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(secretE_Ti, 2)).toString(2), tupleBits));
      }
    }
    if (i == 0)
      System.out.println("Lip1: " + T_i.substring(j_2*d_ip1, (j_2+1)*d_ip1).equals(Lip1));
    else if (i < h)
      System.out.println("Lip1: " + T_i.substring(1+nBits+lBits).substring(j_2*d_ip1, (j_2+1)*d_ip1).equals(Lip1));
    System.out.println("----------- DONE -------------");
    //////////////////////// above are for checking correctness /////////////////////
    
    sanityCheck();
    // C outputs Lip1, secretC_Ti, secretC_P_p
    return new AOutput(Lip1, null, secretC_Ti, null, secretC_P_p, null, d);
  }
  
  @Override
  public AOutput executeDebbieSubTree(Communication charlie, Communication eddie,
                                      BigInteger k, Tree OT, String unused) {
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
    (new DecryptPath()).executeDebbieSubTree(charlie, eddie, k, OT, null);
    // DecryptPath outpus sigma and secretE_P for E and secretC_P for C
    
    if (i > 0) {
      sanityCheck();
      PET.executeDebbie(charlie, eddie, pathTuples);
      // PET outputs j_1 for C
      
      sanityCheck();
      // step 4
      AOT.executeD(charlie, eddie);
    }
    
    // step 5
    if (i < h) {
      // AOT(E, C, D)
      sanityCheck();
      AOT.executeD(charlie, eddie); 
      // outputs ybar_j2 for C
    }
    
    sanityCheck();
    return new AOutput();
  }
  
  @Override
  public AOutput executeEddieSubTree(Communication charlie, Communication debbie,
                                     Tree OT, String unused) {
    // protocol
    // step 1
    // run DecryptPath on C's input Li, E's input OT_i, and D's input k
    DPOutput DecOut = (new DecryptPath()).executeEddieSubTree(charlie, debbie, OT, null);
    String secretE_P = "";
    for (int j=0; j<DecOut.secretE_P.length; j++)
    	secretE_P += DecOut.secretE_P[j];
    // DecryptPath outpus sigma and secretE_P for E and secretC_P for C
    
    ////////////////////////below are for checking correctness /////////////////////
    System.out.println("-----checking correctness-----");
    String Ni = charlie.readString();
    String Li = charlie.readString();
    String secretC_P = charlie.readString();    
    String sigmaPath = Util.addZero(new BigInteger(tupleBits*pathTuples, rnd).toString(2), tupleBits*pathTuples);
    String T_i = "1" + Ni + Li + Util.addZero(new BigInteger(aBits, rnd).toString(2), aBits);
    if (i == 0)
    T_i = Util.addZero(new BigInteger(aBits, rnd).toString(2), aBits);
    int test_j1 = rnd.nextInt(pathTuples);
    sigmaPath = sigmaPath.substring(0, test_j1*tupleBits) + T_i + sigmaPath.substring((test_j1+1)*tupleBits);
    if (i == 0)
    sigmaPath = T_i;            
    secretE_P = Util.addZero(new BigInteger(sigmaPath, 2).xor(new BigInteger(secretC_P, 2)).toString(2), tupleBits*pathTuples);
    //System.out.println("-----done with correctness----");
    //////////////////////// above are for checking correctness /////////////////////

    // step 2
    // party E
    String[] y = new String[twotaupow];
    String y_all;
    if (i == 0) 
      y_all = secretE_P;
    else if (i < h)
      y_all = Util.addZero(new BigInteger(aBits, rnd).toString(2), aBits);
    else // i = h
      y_all = Util.addZero("", aBits);
    for (int o=0; o<twotaupow; o++) {
      y[o] = y_all.substring(o*d_ip1, (o+1)*d_ip1);
    }
    
    String secretE_Ti = "0" + Util.addZero("", i*tau) + Util.addZero ("", d_i) + y_all;
    if (i == 0)
      secretE_Ti = y_all;
    String secretE_P_p = ""; //  i = 0 case
    if (i > 0) { 
      secretE_P_p = secretE_P;
    }
    
    // step 3
    // party C and E
    String[] b = new String[pathTuples];
    if (i > 0) {
      for (int j=0; j<pathTuples; j++) {
        b[j] = secretE_P.substring(j*tupleBits, j*tupleBits+1+nBits); // party E
      }
      sanityCheck();
      PET.executeEddie(charlie, debbie, b);
      // PET outputs j_1 for C
    }
    
    
    // step 4
    // party E
    if (i > 0) {
      String[] e = new String[pathTuples];
      String[] f = new String[pathTuples];
      for (int o=0; o<pathTuples; o++) {
        e[o] = secretE_P.substring(o*tupleBits+1+nBits+lBits, (o+1)*tupleBits);
        f[o] = Util.addZero(new BigInteger(e[o], 2).xor(new BigInteger(y_all, 2)).toString(2), aBits);
      }
      
      sanityCheck();
      // AOT(E, C, D)
      AOT.executeS(charlie, debbie, f);
      // outputs fbar for C
    }
    
    // step 5
    if (i < h) {
      // AOT(E, C, D)
      sanityCheck();
      AOT.executeS(charlie, debbie, y);
      // outputs ybar_j2 for C
    }
    
    ////////////////////////below are for checking correctness /////////////////////
    //System.out.println("Correctness Test Results");
    charlie.write(secretE_Ti);
    charlie.write(T_i);
    charlie.write(test_j1);
    if (i > 0) {
      charlie.write(b);
    }
    ////////////////////////////////////////////////////////////////////////////////
    
    sanityCheck();
    // E outputs secretE_Ti and secretE_P_p
      
    return new AOutput(null, DecOut.p, null, secretE_Ti, null, secretE_P_p, null);
  }

  @Override
  public String prepareArgs() {
    // Nip1 
    // Note: Originally i=0 case has just tau. This should be fine since
    // nBits = i*tau, thus when i=0 nBits = 0 and nBits+tau = tau
    return  Util.addZero(new BigInteger(nBits+tau, rnd).toString(2), nBits+tau); 
  }
}
