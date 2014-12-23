package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.util.Util;

// TODO: Possible parallelization opportunity in running each IOT
public class SSOT extends Operation {

  public SSOT(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public BigInteger[] executeC(Communication I, Communication E, BigInteger[] sC, int length) {
	  IOT iot = new IOT(I, E);
	  
	  I.countBandwidth = false;
	    E.countBandwidth = false;
	    
    //int l = sC[0].length();
	    int l = length; //TODO: remove this?
    I.write(l);
    
    I.countBandwidth = true;
    E.countBandwidth = true;
    I.bandwidth[PID.ssot].start();
    E.bandwidth[PID.ssot].start();
    
    sanityCheck();
    
    // protocol
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
    timing.iot.start();
    BigInteger[] a = iot.executeR(I, E);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    iot.executeS(E, I, sC, length);
    timing.iot.stop();
    
    I.countBandwidth = false;
    E.countBandwidth = false;
    I.bandwidth[PID.ssot].stop();
    E.bandwidth[PID.ssot].stop();
    
    // C outputs a
    return a;
  }
  
  public void executeI(Communication C, Communication E, Integer[] i) throws NoSuchAlgorithmException {
	  IOT iot = new IOT(C, E);
	  
	    E.countBandwidth = false;
	    C.countBandwidth = false;
	    
    // parameters
    int k = i.length;
    int l = C.readInt(); // TODO: Can we make this an input
    
    C.countBandwidth = true;
    E.countBandwidth = true;
    C.bandwidth[PID.ssot].start();
    E.bandwidth[PID.ssot].start();
    
    sanityCheck();
    
    // protocol
    // step 1
    // party I
    timing.ssot_online.start();
    BigInteger[] delta = new BigInteger[k];
    for (int o=0; o<k; o++)
      delta[o] = new BigInteger(l, SR.rand); // TODO: generate once?
    timing.ssot_online.stop();
    
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
    timing.iot.start();
    iot.executeI(C, E, i, delta);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    iot.executeI(E, C, i, delta);
    timing.iot.stop();
    
    E.countBandwidth = false;
    C.countBandwidth = false;
    E.bandwidth[PID.ssot].stop();
    C.bandwidth[PID.ssot].stop();
  }
  
  public BigInteger[] executeE(Communication C, Communication I, BigInteger[] sE, int length) {
	  IOT iot = new IOT(C, I);
	  
	    I.countBandwidth = false;
	    C.countBandwidth = false;
	    
    //int l = sE[0].length();
	  
	  I.countBandwidth = true;
	    C.countBandwidth = true;
	    I.bandwidth[PID.ssot].start();
	    C.bandwidth[PID.ssot].start();
	    
	    sanityCheck();
    
	  // protocol
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
	  timing.iot.start();
    iot.executeS(C, I, sE, length);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    BigInteger[] b = iot.executeR(I, C);
    timing.iot.stop();
    
    I.countBandwidth = false;
    C.countBandwidth = false;
    I.bandwidth[PID.ssot].stop();
    C.bandwidth[PID.ssot].stop();
    
    // E outputs b
    return b;
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
	  /*
    switch (party) {
    case Charlie:
      String[] sC = new String[]{"000", "001", "010", "011", "100", "101", "110", "111"};
      String[] a = SSOT.executeC(con1, con2, sC);
      System.out.println(Arrays.toString(a));

      break;
    case Debbie: // I
      Integer[] i = new Integer[]{0, 1, 3, 6};
      try {
        SSOT.executeI(con1, con2, i);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
      break;
    case Eddie:
      String[] sE = new String[]{"000", "000", "000", "000", "000", "000", "000", "000"};
      String[] b = SSOT.executeE(con1, con2, sE);
      System.out.println(Arrays.toString(b));
      break;
    }
    
    // Ensure we don't close the connection before processing is finished
    con1.write("finished");
    con2.write("finished");
    con1.readString();
    con2.readString();
    */
  }

}
