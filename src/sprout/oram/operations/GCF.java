package sprout.oram.operations;

import java.math.BigInteger;

import YaoGC.Circuit;
import YaoGC.F2ET_Wplus2_Wplus2;
import YaoGC.F2FT_2Wplus2_Wplus2;
import YaoGC.State;
import YaoGC.Wire;
import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.util.Util;

public class GCF extends Operation {
  public GCF(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public static void executeE(Communication C, Communication D, String circuit, int n, String sE) {
	  // TODO: this line is only for checking correctness; should be removed for ORAM
	  D.write(sE);
    
    // setup circuit
	int w = n - 2;
	if (circuit.equals("F2FT"))
		w /= 2;
	int tmp1 = rnd.nextInt(w) + 1;
	int tmp2 = rnd.nextInt(w) + 1;
	int s1 = Math.min(tmp1, tmp2);
	int s2 = Math.max(tmp1, tmp2);
	System.out.println("--- E: sigma:\t" + s1 + " " + s2);
	
	Circuit gc_E = null;
	Circuit.isForGarbling = true;
	if (circuit.equals("F2ET"))
		gc_E = new F2ET_Wplus2_Wplus2(w, s1, s2);
	else
		gc_E = new F2FT_2Wplus2_Wplus2(w, s1, s2);
	Circuit.setReceiver(D);
	try {
		gc_E.build();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	// generate label pairs
	BigInteger[][] lbs = new BigInteger[n][2];
	for (int i = 0; i < n; i++) {
	    BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
	    BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
	    lbs[i][0] = glb0;
	    lbs[i][1] = glb1;
	}
	
	// protocol
	// step 1
	BigInteger[][] A = new BigInteger[n][2];
	BigInteger[] K_E = new BigInteger[n];
	for (int i=0; i<n; i++) {
		int alpha = Character.getNumericValue(sE.charAt(i));
		A[i][0] = lbs[i][alpha];
		A[i][1] = lbs[i][1-alpha];
		C.write(A[i]);
		K_E[i] = lbs[i][0];
	}
	
	// step 3
	State in_E = State.fromLabels(K_E);
	gc_E.startExecuting(in_E);
	
	// interpret output
	// TODO: should not have this round
	BigInteger[] outLbs_D = D.readBigIntegerArray();
	String out = null;
	try {
		out = gc_E.interpretOutputELabels(outLbs_D).toString(2);
	} catch (Exception e) {
		e.printStackTrace();
	}
	if (circuit.equals("F2ET"))
		out = Util.addZero(out, n);
	else
		out = Util.addZero(out, w+2);
	D.write(out);
  }
  
  public static void executeC(Communication D, Communication E, int n, String sC) {
	  // TODO: this line is only for checking correctness; should be removed for ORAM
	  D.write(sC);
	  
	  // protocol
	  // step 1, 2
	  BigInteger[][] A = new BigInteger[n][2];
	  BigInteger[] K_C = new BigInteger[n];
	  for (int i=0; i<n; i++) {
		  A[i] = E.readBigIntegerArray();
		  int beta = Character.getNumericValue(sC.charAt(i));
		  K_C[i] = A[i][beta];
	  }
	  D.write(K_C);
  }
  
  public static String executeD(Communication C, Communication E, String circuit, int n) {
	  // TODO: these lines are only for checking correctness; should be removed for ORAM
	  String sE = E.readString();
	  String sC = C.readString();
	  String input = Util.addZero(new BigInteger(sE, 2).xor(new BigInteger(sC, 2)).toString(2), n);
	  System.out.println("--- D: input:\t" + input);
	  
	  // setup circuit
	  int w = n - 2;
	  if (circuit.equals("F2FT"))
		  w /= 2;
	  
	  Circuit.isForGarbling = false;
	  Circuit gc_D = null;
	  if (circuit == "F2ET")
		  gc_D = new F2ET_Wplus2_Wplus2(w, 1, 1);
	  else
		  gc_D = new F2FT_2Wplus2_Wplus2(w, 1, 1);
	  Circuit.setSender(E);
	  try {
		gc_D.build();
	  } catch (Exception e) {
		e.printStackTrace();
	  }
	  
	  // protocol
	  // step 2
	  BigInteger[] K_C = C.readBigIntegerArray();
	  
	  // step 3
	  State in_D = State.fromLabels(K_C);
	  State out_D = gc_D.startExecuting(in_D);
	  BigInteger[] outLbs_D = out_D.toLabels();
	  
	  // interpret output
	  // TODO: should not have this round
	  E.write(outLbs_D);	  
	  String out = E.readString();
	  return out;
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
 // for testing
	  
	  int n = 18;
	  String circuit = "F2ET";
	  //String circuit = "F2FT";
	  String sC = null, sE = null;
	  String output = null;
    
    switch (party) {
    case Charlie:
      if (circuit == "F2ET")
    	sC = "00" + Util.addZero(new BigInteger(n-2, rnd).toString(2), n-2);
      else
    	sC = "0011111111" + Util.addZero(new BigInteger(n-10, rnd).toString(2), n-10);
      GCF.executeC(con1, con2, n, sC);
      break;
    case Debbie:
      output = GCF.executeD(con1, con2, circuit, n);
      System.out.println("--- D: output:\t" + output);
      break;
    case Eddie:
      sE = Util.addZero("", n);
      GCF.executeE(con1, con2, circuit, n, sE);
      break;
    }
    
    System.out.println("Run completed");
    
  }
}