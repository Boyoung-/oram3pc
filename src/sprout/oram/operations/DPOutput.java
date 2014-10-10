package sprout.oram.operations;

import java.util.ArrayList;
import java.util.List;

public class DPOutput {
  public String secretC_P;
  public String secretE_P;
  List<Integer> p;
  
  DPOutput() {
  }
  
  DPOutput(String c, String e, List<Integer> per) {
    secretC_P = c;
    secretE_P = e;
    
    if (per != null) {
      p = new ArrayList<Integer>(per);
    }
  }
  
  @Override
  public String toString() {
    String out =  "";
    if (secretC_P != null)
      out += "secretC_P: " + secretC_P + "\n";
    if (secretE_P != null)
      out += "secretE_P: " + secretE_P + "\n";
    if (p != null)
      out += "p: " + p.toString() + "\n";
    
    return out;
  }
}
