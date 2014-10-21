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
    System.out.println("--- E: hello");
    
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
	// TODO: add communication
	// Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
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
	State out_E = gc_E.startExecuting(in_E);
	BigInteger[] outLbs_E = out_E.toLabels();
	D.write(outLbs_E);
  }
  
  public static void executeC(Communication D, Communication E, int n, String sC) {
	  System.out.println("--- C: hello");
	  
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
	  System.out.println("--- D: hello");
	  
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
	  // TODO: add communication
	  //Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
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
	  BigInteger[] outLbs_E = E.readBigIntegerArray();
	  State state_E = State.fromLabels(outLbs_E);
	  
	  BigInteger output = BigInteger.ZERO;
		for (int i = 0; i < outLbs_D.length; i++) {
		    if (state_E.wires[i].value != Wire.UNKNOWN_SIG) {
			if (state_E.wires[i].value == 1)
			    output = output.setBit(i);
			continue;
		    }
		    else if (outLbs_D[i].equals(state_E.wires[i].invd ? 
						 state_E.wires[i].lbl :
						 state_E.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {
			    output = output.setBit(i);
		    }
		    else if (!outLbs_D[i].equals(state_E.wires[i].invd ? 
						  state_E.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
						  state_E.wires[i].lbl))
				try {
					throw new Exception("Bad label encountered: i = " + i + "\t" +
							    outLbs_D[i] + " != (" + 
							    state_E.wires[i].lbl + ", " +
							    state_E.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		String out = output.toString(2);
		if (circuit.equals("F2ET"))
			out = Util.addZero(out, n);
		else
			out = Util.addZero(out, w+2);
		return out;
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
 // for testing
	  
	  int n = 18;
	  String circuit = "F2ET";
	  circuit = "F2FT";
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
      break;
    case Eddie:
      sE = Util.addZero("", n);
      GCF.executeE(con1, con2, circuit, n, sE);
      break;
    }
    
    String input = Util.addZero(new BigInteger(sC, 2).xor(new BigInteger(sE, 2)).toString(2), n);
    System.out.println("--- input:\t" + input);
    System.out.println("--- output:\t" + output);
    
    System.out.println("Run completed");
    
  }
}