package sprout.oram.operations;

import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.SR;

public class EPath {
  ECPoint[] x;
  BigInteger[] Bbar;
  
  EPath(ECPoint[] xx, BigInteger[] bb) {
	  if (xx.length != bb.length)
		try {
			throw new Exception("Lengths not equal");
		} catch (Exception e) {
			e.printStackTrace();
		}
    x = xx.clone();
    Bbar = bb.clone();
  }
  
  // random generation for testing purpose
  EPath(int n, int l) {
    x = new ECPoint[n];
    Bbar = new BigInteger[n];
    for (int i=0; i<n; i++) {
      x[i] = OPRFHelper.getOPRF().randomPoint();
      Bbar[i] = new BigInteger(l, SR.rand);
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