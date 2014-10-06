package sprout.oram.operations;

import java.util.ArrayList;
import java.util.List;

public class AOutput {
  String Lip1;
  List<Integer> p;
  String secretC_Ti;
  String secretE_Ti;
  String secretC_P_p;
  String secretE_P_p;
  String data;
  
  AOutput() {}
  
  AOutput(String l, List<Integer> per, String ct, String et, String cp, String ep, String d) {
    Lip1 = l;
    p = new ArrayList<Integer>(per);
    secretC_Ti = ct;
    secretE_Ti = et;
    secretC_P_p = cp;
    secretE_P_p = ep;
    data = d;
  }
}