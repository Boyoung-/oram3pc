package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.Forest.TreeZero;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.ui.CryptoParam;
import sprout.util.Util;

public class EncryptPath extends TreeOperation<EPath, String> {

  EncryptPath(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }

  @Override
  public EPath executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, TreeZero OT_0, Tree OT, String secretC_P) {
    // Step 1
    // D sends s and x to E
    // D sends c to C
    String[] c = debbie.readStringArray();
    
    // step 2
    // party C
    String[] secretC_B = new String[n];
    String[] d = new String[n];
    for (int j=0; j<n; j++) {
      secretC_B[j] = secretC_P.substring(j*l, (j+1)*l);
      d[j] = Util.addZero(new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2), l);
    }
    // C sends d to E
    eddie.write(d);
    
    return null;
  }

  @Override
  public EPath executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger k, TreeZero OT_0, Tree OT,
      String unused) {
    try {
      // protocol
      // step 1
      // party D
      BigInteger y = CryptoParam.g.modPow(k, CryptoParam.p);
      byte[] s = rnd.generateSeed(16);  // 128 bits
      BigInteger[] r = new BigInteger[n];
      BigInteger[] x = new BigInteger[n];
      BigInteger[] v = new BigInteger[n];
      for (int j=0; j<n; j++) {
        r[j] = Util.randomBigInteger(CryptoParam.q);
        x[j] = CryptoParam.g.modPow(r[j], CryptoParam.p);
        v[j] = y.modPow(r[j], CryptoParam.p);
      }
      PRG G1 = new PRG(l*(n));
      String a_all = G1.generateBitString(l*(n), s);
      String[] a = new String[n];
      String[] b = new String[n];
      String[] c = new String[n];
      for (int j=0; j<n; j++) {
        a[j] = a_all.substring(j*l, (j+1)*l);
        PRG G2 = new PRG(l); // non-fresh generated SecureRandom cannot guarantee determinism... (why???)
        b[j] = G2.generateBitString(l, v[j]);
        c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2), l);
      }
      // D sends s and x to E
      // D sends c to C
      eddie.write(s);
      eddie.write(x);
      charlie.write(c);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error in EncryptPath charlie and eddie will probably hang");
    }
    
    return null;
  }

  @Override
  public EPath executeEddieSubTree(Communication charlie,
      Communication debbie, TreeZero OT_0, Tree OT, String secretE_P) {
    try {
      // Step 1
      // D sends s and x to E
      // D sends c to C
      byte[] s = debbie.read();
      BigInteger[] x = debbie.readBigIntegerArray();

      // Step 2
      // C sends d to E
      String[] d = charlie.readStringArray();

      // step 3
      // party E
      // generation of a[], TODO: boyang can you double check this?

      PRG G1 = new PRG(l*(n));
      String a_all = G1.generateBitString(l*(n), s);
      String[] a = new String[n];
      for (int j=0; j<n; j++) {
        a[j] = a_all.substring(j*l, (j+1)*l);
      }

      // end generation of a[]
      String[] secretE_B = new String[n];
      String[] Bbar = new String[n];
      for (int j=0; j<n; j++) {
        secretE_B[j] = secretE_P.substring(j*l, (j+1)*l);
        Bbar[j] = Util.addZero(new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2), l);
      }
      
      // E outputs encrypted path
      return new EPath(x, Bbar);
    } catch (Exception e){ 
      e.printStackTrace();
      System.out.println("Exception in EncryptPath Eddie, others probably hanging");
      return null;
    }
  }
  
  //Temporarily redefine n
  // We probably want to eventually unify the meaning of n
 @Override
 public void loadTreeSpecificParameters(Tree OT) {
   super.loadTreeSpecificParameters(OT);
   n = n/w;
 }

  @Override
  public String prepareArgs() {
    int ldata         = twotaupow * metadata.getTupleBitsL(h-1);
    return Util.addZero(new BigInteger(ldata, rnd).toString(2), ldata);
  }
}
