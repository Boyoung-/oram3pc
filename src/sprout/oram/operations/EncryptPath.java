package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.oprf.OPRF;
import sprout.oram.Tree;
import sprout.util.Util;

public class EncryptPath extends TreeOperation<EPath, String> {

	/*
  EncryptPath(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }
  */
	
  public EncryptPath(Communication con1, Communication con2) {
    super(con1, con2);
  }

  @Override
  public EPath executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, Tree OT, String secretC_P) {
    // Step 1
    // D sends s and x to E
    // D sends c to C
    String[] c = debbie.readStringArray();
    
    // step 2
    // party C
    String[] secretC_B = new String[pathBuckets];
    String[] d = new String[pathBuckets];
    for (int j=0; j<pathBuckets; j++) {
      secretC_B[j] = secretC_P.substring(j*bucketBits, (j+1)*bucketBits);
      d[j] = Util.addZero(new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2), bucketBits);
    }
    // C sends d to E
    eddie.write(d);
    
    return null;
  }

  @Override
  public EPath executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger k, Tree OT,
      String unused) {
    try {
      OPRF oprf = OPRFHelper.getOPRF(false);
      // protocol
      // step 1
      // party D
      //ECPoint y = oprf.getY();
      byte[] s = rnd.generateSeed(16);  // 128 bits
      //BigInteger[] r = new BigInteger[pathBuckets];
      ECPoint[] x = new ECPoint[pathBuckets];
      ECPoint[] v = new ECPoint[pathBuckets];
      BigInteger r;
      for (int j=0; j<pathBuckets; j++) {
        // This computation is repeated in oprf in some form
        r = oprf.randomExponent();
        x[j] = oprf.getG().multiply(r);
        v[j] = oprf.getY().multiply(r);
        // below is a version using only the exposed methods, however the above should eventually be used
        // once same base optimizations are implemented
        // x[j] = oprf.randomPoint();
        // v[j] = oprf.evaluate(x[j]).getResult();
      }
      PRG G1 = new PRG(bucketBits*pathBuckets);
      String a_all = G1.generateBitString(bucketBits*pathBuckets, s);
      String[] a = new String[pathBuckets];
      String[] b = new String[pathBuckets];
      String[] c = new String[pathBuckets];
      for (int j=0; j<pathBuckets; j++) {
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
        PRG G2 = new PRG(bucketBits); // non-fresh generated SecureRandom cannot guarantee determinism... (why???)
        b[j] = G2.generateBitString(bucketBits, v[j]);
        c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2), bucketBits);
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
      Communication debbie, Tree OT, String secretE_P) {
    try {
      // Step 1
      // D sends s and x to E
      // D sends c to C
      byte[] s = debbie.read();
      ECPoint[] x = debbie.readECPointArray();

      // Step 2
      // C sends d to E
      String[] d = charlie.readStringArray();

      // step 3
      // party E
      // regeneration of a[]
      PRG G1 = new PRG(bucketBits*(pathBuckets));
      String a_all = G1.generateBitString(bucketBits*(pathBuckets), s);
      String[] a = new String[pathBuckets];
      for (int j=0; j<pathBuckets; j++) {
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
      }
      // end generation of a[]
      
      String[] secretE_B = new String[pathBuckets];
      String[] Bbar = new String[pathBuckets];
      for (int j=0; j<pathBuckets; j++) {
        secretE_B[j] = secretE_P.substring(j*bucketBits, (j+1)*bucketBits);
        Bbar[j] = Util.addZero(new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2), bucketBits);
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
 public void loadTreeSpecificParameters(int index) {
   super.loadTreeSpecificParameters(index);
   //if (i > 0)
	   //n = n/w;
 }

  @Override
  public String prepareArgs() {
	  /*
    int length;
    if (i == 0) {
      length = bucketBits;
    } else {
      length = bucketBits * pathBuckets;
    }
    */
	  int length = bucketBits * pathBuckets;
    return Util.addZero(new BigInteger(length, rnd).toString(2), length);
  }
}
