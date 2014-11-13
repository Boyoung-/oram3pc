package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.oprf.OPRF;
import sprout.oram.PID;
import sprout.oram.Tree;
import sprout.util.Util;

public class EncryptPath extends TreeOperation<EPath, String> {

	public EncryptPath() {
		super(null, null);
	}
	
  public EncryptPath(Communication con1, Communication con2) {
    super(con1, con2);
  }

  @Override
  public EPath executeCharlieSubTree(Communication debbie, Communication eddie, 
		  String unused1, Tree unused2, String secretC_P) {
	  debbie.countBandwidth = true;
	    eddie.countBandwidth = true;
	    debbie.bandwidth[PID.encrypt].start();
	    eddie.bandwidth[PID.encrypt].start();
	  
	  // protocol
    // step 1
    // D sends s and x to E
    // D sends c to C
	  timing.encrypt_read.start();
    byte[][] c = debbie.readDoubleByteArray();
    timing.encrypt_read.stop();
    
    // step 2
    // party C
    String[] secretC_B = new String[pathBuckets];
    byte[][] d = new byte[pathBuckets][];
    timing.encrypt_online.start();
    for (int j=0; j<pathBuckets; j++) {
      secretC_B[j] = secretC_P.substring(j*bucketBits, (j+1)*bucketBits);
      d[j] = new BigInteger(1, c[j]).xor(new BigInteger(secretC_B[j], 2)).toByteArray();
    }
    timing.encrypt_online.stop();
    // C sends d to E
    timing.encrypt_write.start();
    eddie.write(d);
    timing.encrypt_write.stop();
    
    debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    debbie.bandwidth[PID.encrypt].stop();
    eddie.bandwidth[PID.encrypt].stop();
    
    return null;
  }

  @Override
  public EPath executeDebbieSubTree(Communication charlie, Communication eddie, 
		  BigInteger k, Tree unused1, String unused2) {
	  charlie.countBandwidth = true;
	  eddie.countBandwidth = true;	  
	  charlie.bandwidth[PID.encrypt].start();
	  eddie.bandwidth[PID.encrypt].start();
	  
    try {
      OPRF oprf = OPRFHelper.getOPRF(false);
      // protocol
      // step 1
      // party D
      //ECPoint y = oprf.getY();
      //BigInteger[] r = new BigInteger[pathBuckets];
      ECPoint[] x = new ECPoint[pathBuckets];
      ECPoint[] v = new ECPoint[pathBuckets];
      BigInteger r;
      PRG G1 = new PRG(bucketBits*pathBuckets);
      String[] a = new String[pathBuckets];
      byte[][] b = new byte[pathBuckets][];
      byte[][] c = new byte[pathBuckets][];
      
      timing.encrypt_online.start();
      byte[] s = rnd.generateSeed(16);  // 128 bits
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
      String a_all = G1.generateBitString(bucketBits*pathBuckets, s);
      for (int j=0; j<pathBuckets; j++) {
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
        PRG G2 = new PRG(bucketBits); // non-fresh generated SecureRandom cannot guarantee determinism... (why???)
        b[j] = G2.generateBytes(bucketBits, v[j]);
        c[j] = new BigInteger(a[j], 2).xor(new BigInteger(1, b[j])).toByteArray();
      }
      timing.encrypt_online.stop();
      // D sends s and x to E
      // D sends c to C
      timing.encrypt_write.start();
      eddie.write(s);
      eddie.write(x);
      charlie.write(c);
      timing.encrypt_write.stop();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error in EncryptPath charlie and eddie will probably hang");
    }
    
    charlie.countBandwidth = false;
	  eddie.countBandwidth = false;	  
	  charlie.bandwidth[PID.encrypt].stop();
	  eddie.bandwidth[PID.encrypt].stop();
    
    return null;
  }

  @Override
  public EPath executeEddieSubTree(Communication charlie, Communication debbie, 
		  Tree unused, String secretE_P) {
	  charlie.countBandwidth = true;
	  debbie.countBandwidth = true;
	  charlie.bandwidth[PID.encrypt].start();
	  debbie.bandwidth[PID.encrypt].start();
	  
    try {
    	// protocol
      // step 1
      // D sends s and x to E
      // D sends c to C
    	timing.encrypt_read.start();
      byte[] s = debbie.read();
      ECPoint[] x = debbie.readECPointArray();

      // Step 2
      // C sends d to E
      byte[][] d = charlie.readDoubleByteArray();
      timing.encrypt_read.stop();

      // step 3
      // party E
      // regeneration of a[]
      PRG G1 = new PRG(bucketBits*pathBuckets);
      String[] a = new String[pathBuckets];
      timing.encrypt_online.start();
      String a_all = G1.generateBitString(bucketBits*pathBuckets, s);
      for (int j=0; j<pathBuckets; j++) {
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
      }
      timing.encrypt_online.stop();
      // end generation of a[]
      
      String[] secretE_B = new String[pathBuckets];
      BigInteger[] Bbar = new BigInteger[pathBuckets];
      timing.encrypt_online.start();
      for (int j=0; j<pathBuckets; j++) {
        secretE_B[j] = secretE_P.substring(j*bucketBits, (j+1)*bucketBits);
        Bbar[j] = new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(1, d[j]));
      }
      timing.encrypt_online.stop();
      
      charlie.countBandwidth = false;
	  debbie.countBandwidth = false;
	  charlie.bandwidth[PID.encrypt].stop();
	  debbie.bandwidth[PID.encrypt].stop();
      
      // E outputs encrypted path
      return new EPath(x, Bbar);
    } catch (Exception e){ 
      e.printStackTrace();
      System.out.println("Exception in EncryptPath Eddie, others probably hanging");
      return null;
    }
  }

  @Override
  public String prepareArgs() {
	  int length = bucketBits * pathBuckets;
    return Util.addZero(new BigInteger(length, rnd).toString(2), length);
  }
}
