package sprout.oram.operations;

import java.util.ArrayList;
import java.util.List;

import sprout.util.Util;

public class DPOutput {
  //public String[] secretC_P;
  //public String[] secretE_P;
	public byte[][] secretC_P;
	public byte[][] secretE_P;
  List<Integer> p;
  
  DPOutput() {
  }
  
  //DPOutput(String[] c, String[] e, List<Integer> per) {
  DPOutput(byte[][] c, byte[][] e, List<Integer> per) {
	  if (c != null)
		  secretC_P = Util.cloneMatrix(c);
	  if (e != null)
		  secretE_P = Util.cloneMatrix(e);
    if (per != null) 
      p = new ArrayList<Integer>(per);
  }
  
  /*
  @Override
  public String toString() {
    String out =  "";
    if (secretC_P != null)
    	for (int i=0; i<secretC_P.length; i++)
    		out += "secretC_P " + i + ": " + secretC_P[i] + "\n";
    if (secretE_P != null)
      for (int i=0; i<secretE_P.length; i++)
    	  out += "secretE_P " + i + ": " + secretE_P[i] + "\n";
    if (p != null)
      out += "p: " + p.toString() + "\n";
    
    return out;
  }
  */
}
