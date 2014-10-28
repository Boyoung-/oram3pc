package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.util.Util;

// TODO: Possible parallelization opportunity in running each IOT
public class SSOT extends Operation {

  public SSOT(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public static String[] executeC(Communication I, Communication E, String[] sC) {
    int l = sC[0].length();
    I.write(l);
    
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
    String[] a = IOT.executeR(I, E);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    IOT.executeS(E, I, sC);
    
    // C outputs a
    return a;
  }
  
  public static void executeI(Communication C, Communication E, Integer[] i) throws NoSuchAlgorithmException {
    // parameters
    int k = i.length;
    int l = C.readInt(); // TODO: Can we make this an input
    
    // protocol
    // step 1
    // party I
    String[] delta = new String[k];
    for (int o=0; o<k; o++)
      delta[o] = Util.addZero(new BigInteger(l, rnd).toString(2), l);
    
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
    IOT.executeI(C, E, i, delta);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    IOT.executeI(E, C, i, delta);
  }
  
  public static String[] executeE(Communication C, Communication I, String[] sE) {
    //int l = sE[0].length();
    
    // step 2
    // parties run IOT(E, C, I) on inputs sE for E and i, delta for I
    IOT.executeS(C, I, sE);
    
    // step 3
    // parties run IOT(C, E, I) on inputs sC for C and i, delta for I
    String[] b = IOT.executeR(I, C);
    
    // E outputs b
    return b;
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
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
  }

}
