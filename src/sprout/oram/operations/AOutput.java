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
    if (per != null)
      p = new ArrayList<Integer>(per);
    secretC_Ti = ct;
    secretE_Ti = et;
    secretC_P_p = cp;
    secretE_P_p = ep;
    data = d;
  }
  
  @Override
  public String toString() {
    String out = "";
    
    if (Lip1 != null) {
      out += "Lip1: " + Lip1 + "\n";
    } if (p != null) { 
      out += "P: " + p.toString() + "\n";
    } if (secretC_Ti != null) {
      out += "secretC_Ti: " + secretC_Ti + "\n";
    } if (secretE_Ti != null) {
      out += "secretE_Ti: " + secretE_Ti + "\n";
    } if (secretC_P_p != null) {
      out += "secretC_P_p: " + secretC_P_p + "\n";
    } if (secretE_P_p != null) {
      out += "secretE_P_p: " + secretE_P_p + "\n";
    } if (data != null) {
      out += "data: " + data + "\n";
    }
    
    return out;
  }
}