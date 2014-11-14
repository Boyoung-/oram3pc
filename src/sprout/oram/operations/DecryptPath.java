package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.crypto.oprf.Message;
import sprout.crypto.oprf.OPRF;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.PID;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Util;

public class DecryptPath extends TreeOperation<DPOutput, EPath>{

  public DecryptPath(Communication con1, Communication con2) {
    super(con1, con2);
  }

  @Override
  public DPOutput executeCharlieSubTree(Communication debbie, Communication eddie,
                                        String Li, Tree unused1, EPath unused2) {
	  debbie.countBandwidth = true;
	  eddie.countBandwidth = true;
	  debbie.bandwidth[PID.decrypt].start();
	  eddie.bandwidth[PID.decrypt].start();
	  
	  sanityCheck();
	  
    // protocol
    // step 1
    // party C
    // C sends Li to E
	  if (lBits > 0) {
		  timing.decrypt_online.start();
		  byte[] Li_byte = new BigInteger(Li, 2).toByteArray();
		  timing.decrypt_online.stop();
		  
		  timing.decrypt_write.start();
	    eddie.write(Li_byte); 
	    timing.decrypt_write.stop();
	  }
    
    // step 3   
    // party C
    // E sends sigma_x to C
	  //sanityCheck(eddie);
    timing.decrypt_read.start();
    ECPoint[] sigma_x = eddie.readECPointArray();
    timing.decrypt_read.stop();
    //System.out.println("--- D: sigma_x: " + sigma_x.length);
   

	  debbie.bandwidth[PID.oprf].start();
	  eddie.bandwidth[PID.oprf].start();
    // step 4
    // party C and D run OPRF on C's input sigma_x and D's input k
    timing.oprf.start();
    OPRF oprf = OPRFHelper.getOPRF();
    oprf.timing = timing;  // TODO: better way?
    String[] secretC_P = new String[sigma_x.length];
    for (int j=0; j<sigma_x.length; j++) {
      // This oprf should possibly be evaulated in as an Operation
      // For an easier description of the flow look at OPRFTest.java
      // TODO: May want a different encoding here we leave this until OPRF changes
      Message msg1 = oprf.prepare(sigma_x[j]); // contains pre-computation and online computation
      //sanityCheck(debbie);
      timing.oprf_write.start();
      debbie.write(new Message(msg1.getV()));
      timing.oprf_write.stop();

      //sanityCheck(debbie);
      timing.oprf_read.start();
      Message msg2 = debbie.readMessage();
      timing.oprf_read.stop();
      
      timing.oprf_online.start();      
      msg2.setW(msg1.getW());
      Message res = oprf.deblind(msg2);
      timing.oprf_online.stop();

      PRG G;
      try {
        G = new PRG(bucketBits); // TODO: fresh PRG non-deterministic problem?
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
      }

      timing.oprf_online.start();
      secretC_P[j] = G.generateBitString(bucketBits, res.getResult());
      timing.oprf_online.stop();
    }
    timing.oprf.stop();
    
    debbie.bandwidth[PID.oprf].stop();
	  eddie.bandwidth[PID.oprf].stop();
    
    
    debbie.bandwidth[PID.decrypt].stop();
	  eddie.bandwidth[PID.decrypt].stop();
    debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    
    // C outputs secretC_P
    return new DPOutput(secretC_P, null, null);
  }
  
  @Override
  public DPOutput executeDebbieSubTree(Communication charlie, Communication eddie,
                                       BigInteger k, Tree unused1, EPath unused2) {
	  charlie.countBandwidth = true;
	  eddie.countBandwidth = true;
	  charlie.bandwidth[PID.decrypt].start();
	  eddie.bandwidth[PID.decrypt].start();
	  
	  
	  charlie.bandwidth[PID.oprf].start();
	  eddie.bandwidth[PID.oprf].start();
	  
	  sanityCheck();
	  
	  // protocol
	  // step 4
	  timing.oprf.start();
    OPRF oprf = OPRFHelper.getOPRF(false);
    for (int j=0; j < pathBuckets; j++) {
    	//sanityCheck(charlie);
    	timing.oprf_read.start();
      Message msg = charlie.readMessage();
      timing.oprf_read.stop();

      timing.oprf_online.start();
      msg = oprf.evaluate(msg); // TODO: pass k as arg or just read from file?
      timing.oprf_online.stop();

    	//sanityCheck(charlie);
      timing.oprf_write.start();
      charlie.write(msg);
      timing.oprf_write.stop();
    }
    timing.oprf.stop();
    
    charlie.bandwidth[PID.oprf].stop();
	  eddie.bandwidth[PID.oprf].stop();
    
    
    eddie.bandwidth[PID.decrypt].stop();
	  charlie.bandwidth[PID.decrypt].stop();
    charlie.countBandwidth = false;
    eddie.countBandwidth = false;
    
    // D outputs nothing
    return null;
  }
  
  @Override
  public DPOutput executeEddieSubTree(Communication charlie, Communication debbie,
                                      Tree OT, EPath unused1) {	  
	  charlie.countBandwidth = true;
	  debbie.countBandwidth = true;
	  charlie.bandwidth[PID.decrypt].start();
	  debbie.bandwidth[PID.decrypt].start();
	  
	  sanityCheck();
	  
    // protocol
    // step 1
    // party C
    // C sends Li to E
	  String Li = "";
	  
	  if (lBits > 0) {
		  timing.decrypt_read.start();
	    byte[] Li_byte = charlie.read();   
	    timing.decrypt_read.stop();
	    
	    Li = Util.addZero(new BigInteger(1, Li_byte).toString(2), lBits);
	  }
    
    
    // step 2
    // party E
    // E retrieves encrypted path Pbar using Li
    Bucket[] Pbar = null;
	try {
		timing.decrypt_online.start();
		Pbar = OT.getBucketsOnPath(Li);
		timing.decrypt_online.stop();
	} catch (TreeException e) {
		e.printStackTrace();
	} catch (BucketException e) {
		e.printStackTrace();
	}
    
	
    // step 3   
    // party E
    // E sends sigma_x to C  
	timing.decrypt_online.start();
    List<Integer> sigma = new ArrayList<Integer>(); 
    for (int j=0; j<Pbar.length; j++)
      sigma.add(j);
    Collections.shuffle(sigma, SR.rand);
    
    ECPoint[] x = new ECPoint[Pbar.length];
    String[] Bbar = new String[Pbar.length];  
    for (int j=0; j<Pbar.length; j++) { 
    	x[j] = Util.byteArrayToECPoint(Pbar[j].getNonce());
    	Bbar[j] = Util.addZero(new BigInteger(1, Pbar[j].getByteTuples()).toString(2), bucketBits);
    }
    ECPoint[] sigma_x = Util.permute(x, sigma);
    String[] secretE_P = Util.permute(Bbar, sigma);
	timing.decrypt_online.stop();
	
    //sanityCheck(charlie);
	timing.decrypt_write.start();
    charlie.write(sigma_x);
    timing.decrypt_write.stop();
    

	  debbie.bandwidth[PID.decrypt].stop();
	  charlie.bandwidth[PID.decrypt].stop();
    charlie.countBandwidth = false;
    debbie.countBandwidth = false;
    
    // E outputs sigma and secretE_P    
    return new DPOutput(null, secretE_P, sigma);
  }
  
  @Override
  public EPath prepareArgs() {
	  return null;
	  /*
    if (i==0)
      return null;
    return new EPath(n, l);
    */
  }
}
