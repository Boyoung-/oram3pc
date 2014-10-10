package sprout.oram.operations;

import java.math.BigInteger;
import java.util.Arrays;

import sprout.ui.CryptoParam;
import sprout.util.Util;

public class EPath {
  BigInteger[] x;
  String[] Bbar;
  
  EPath(BigInteger[] xx, String[] bb) {
    x = xx.clone();
    Bbar = bb.clone();
  }
  
  // random generation for testing purpose
  EPath(int n, int l) {
    x = new BigInteger[n];
    Bbar = new String[n];
    for (int i=0; i<n; i++) {
      x[i] = Util.randomBigInteger(CryptoParam.q);
      Bbar[i] = Util.addZero(new BigInteger(l, TreeOperation.rnd).toString(2), l);
    }
  }
  
  @Override
  public String toString() {
    String out = "";
    if (x != null)
      out += "x: " + Arrays.toString(x) + "\n";
    if (Bbar != null)
      out += "Bbar: " + Arrays.toString(Bbar) + "\n";
    
    return out;
  }
}