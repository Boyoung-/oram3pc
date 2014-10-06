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
    p = new ArrayList<Integer>(per);
  }
}
