package sprout.oram.operations;

import java.math.BigInteger;

import Cipher.Cipher;
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
	  // this line is only for checking correctness; should be removed for real execution
	  //D.write(sE);
    
    // setup circuit
	int w = n - 2;
	if (circuit.equals("F2FT"))
		w /= 2;
	int tmp1 = rnd.nextInt(w) + 1;
	int tmp2 = rnd.nextInt(w) + 1;
	int s1 = Math.min(tmp1, tmp2);
	int s2 = Math.max(tmp1, tmp2);
	//System.out.println("--- E: sigma:\t" + s1 + " " + s2);
	
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
	for (int i=0; i<gc_E.outputWires.length; i++) // TODO: not a good way; should define a function
		gc_E.outputWires[i].outBitEncPair = new BigInteger[2];
	
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
		timing.gcf_write.start();
		C.write(A[i]);
		timing.gcf_write.stop();
		K_E[i] = lbs[i][0];
	}
	
	// step 3
	State in_E = State.fromLabels(K_E);
	timing.gcf_online.start(); // TODO: separate gcf write/read time out of this!
	gc_E.startExecuting(in_E);
	timing.gcf_online.stop();
  }
  
  public static void executeC(Communication D, Communication E, int n, String sC) {
	  // this line is only for checking correctness; should be removed for real execution
	  //D.write(sC);
	  
	  // protocol
	  // step 1, 2
	  BigInteger[][] A = new BigInteger[n][2];
	  BigInteger[] K_C = new BigInteger[n];
	  for (int i=0; i<n; i++) {
		  timing.gcf_read.start();
		  A[i] = E.readBigIntegerArray();
		  timing.gcf_read.stop();
		  int beta = Character.getNumericValue(sC.charAt(i));
		  K_C[i] = A[i][beta];
	  }
	  timing.gcf_write.start();
	  D.write(K_C);
	  timing.gcf_write.stop();
  }
  
  public static String executeD(Communication C, Communication E, String circuit, int n) {
	  // these lines are only for checking correctness; should be removed for real execution
	  //String sE = E.readString();
	  //String sC = C.readString();
	  //String input = Util.addZero(new BigInteger(sE, 2).xor(new BigInteger(sC, 2)).toString(2), n);
	  //System.out.println("--- D: input:\t" + input);
	  
	  // setup circuit
	  int w = n - 2;
	  if (circuit.equals("F2FT"))
		  w /= 2;

	  Circuit gc_D = null;
	  Circuit.isForGarbling = false;
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
	  for (int i=0; i<gc_D.outputWires.length; i++) // TODO: not a good way; should define a function
			gc_D.outputWires[i].outBitEncPair = new BigInteger[2];
	  
	  // protocol
	  // step 2
	  timing.gcf_read.start();
	  BigInteger[] K_C = C.readBigIntegerArray();
	  timing.gcf_read.stop();
	  
	  // step 3
	  State in_D = State.fromLabels(K_C);
	  timing.gcf_online.start(); // TODO: separate gcf write/read time out of this!
	  gc_D.startExecuting(in_D);
	  timing.gcf_online.stop();
	  
	  BigInteger output = BigInteger.ZERO;
	  int length = gc_D.outputWires.length;
	  timing.gcf_online.start();
	  for (int i=0; i<length; i++) {
		  BigInteger lb = gc_D.outputWires[i].lbl;
		  int lsb = lb.testBit(0) ? 1 : 0;
		  int k = gc_D.outputWires[i].serialNum;
		  int outBit = Cipher.decrypt(k, lb, gc_D.outputWires[i].outBitEncPair[lsb]);
		  if (outBit == 1)
			  output = output.setBit(length-1-i);
	  }
	  timing.gcf_online.stop();
	  
	  String out = output.toString(2);
		if (circuit.equals("F2ET"))
			out = Util.addZero(out, n);
		else
			out = Util.addZero(out, w+2);
		//System.out.println("--- D: output:\t" + out);
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