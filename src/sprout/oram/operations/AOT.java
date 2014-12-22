package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.AES_PRF;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.util.Util;

public class AOT extends Operation {
  public AOT(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public void executeE(Communication C, Communication D, BigInteger[] m, int mLength) {
	    C.countBandwidth = false;
	    D.countBandwidth = false;
	  
    int N = m.length;
    //int l = m[0].length();
    int l = mLength;
    
    // We may be able to do this without communication
    C.write(N);
    C.write(l);
    //D.write(N);
    D.write(l);
    
    // pre-computed input
    // party E
    byte[] k = new byte[16];
    SR.rand.nextBytes(k);
    // E sends k to D
    D.write(k);
    
    C.countBandwidth = true;
    D.countBandwidth = true;
    C.bandwidth[PID.aot].start();
	D.bandwidth[PID.aot].start();
	
	sanityCheck();
    
    // step 1
    // party E
    timing.aot_online.start();
    BigInteger alpha = BigInteger.valueOf(SR.rand.nextInt(N));
    timing.aot_online.stop();
    BigInteger[] m_p = new BigInteger[N];
    try {
  	  AES_PRF f = new AES_PRF(l);
  	  f.init(k);
      timing.aot_online.start();
      for (int t=0; t<N; t++) {
    	  m_p[t] = new BigInteger(1, f.compute(BigInteger.valueOf(t).add(alpha).mod(BigInteger.valueOf(N)).toByteArray())).xor(m[t]);
      }
      timing.aot_online.stop();
    } catch (Exception e){
      e.printStackTrace();
    }
    
    //sanityCheck(C);
    
    timing.aot_write.start();
    C.write(m_p);
    C.write(alpha);
    timing.aot_write.stop();
    // E sends m_p and alpha to C
    
    C.bandwidth[PID.aot].stop();
	D.bandwidth[PID.aot].stop();
    C.countBandwidth = false;
    D.countBandwidth = false;
  }
  
  public BigInteger executeC(Communication D, Communication E, int j) {
	    D.countBandwidth = false;
	    E.countBandwidth = false;
	    
    int N = E.readInt();
    int l = E.readInt();
    
    D.countBandwidth = true;
    E.countBandwidth = true;
    E.bandwidth[PID.aot].start();
	D.bandwidth[PID.aot].start();
	
	sanityCheck();
	
	//sanityCheck(E);
    // step 1
    // E sends m_p and alpha to C
    timing.aot_read.start();
    BigInteger [] m_p = E.readBigIntegerArray();
    BigInteger alpha = E.readBigInteger();
    timing.aot_read.stop();
    
    // step 2
    //party C
    timing.aot_online.start();
    BigInteger j_p = BigInteger.valueOf(j).add(alpha).mod(BigInteger.valueOf(N));
    timing.aot_online.stop();
    
    //sanityCheck(D);
    
    // C sends j_p to D
    timing.aot_write.start();
    D.write(j_p);
    timing.aot_write.stop();
    
    // step 3
    // D sends c to C
    //sanityCheck(D);
    timing.aot_read.start();
    BigInteger c = D.readBigInteger();
    timing.aot_read.stop();
    
    timing.aot_online.start();
    //String output = Util.addZero(c.xor(m_p[j]).toString(2), l);
    BigInteger output = c.xor(m_p[j]);
    timing.aot_online.stop();
    // C outputs output
    
    D.bandwidth[PID.aot].stop();
	E.bandwidth[PID.aot].stop();
    D.countBandwidth = false;
    E.countBandwidth = false;
    
    return output;
  }
  
  public void executeD(Communication C, Communication E) {
	    C.countBandwidth = false;
	    E.countBandwidth = false;
	    
    //int N = E.readInt();
    int l = E.readInt();
    
    // pre-computed input
    byte[] k = E.read();
    
    C.countBandwidth = true;
    E.countBandwidth = true;
    C.bandwidth[PID.aot].start();
	E.bandwidth[PID.aot].start();
	
	sanityCheck();
    
    // protocol
	//sanityCheck(C);
    // step 2
    // C sends j_p to D
    timing.aot_read.start();
    BigInteger j_p = C.readBigInteger();
    timing.aot_read.stop();
    
    // step 3
    // party D
    try {
    	AES_PRF f = new AES_PRF(l);
    	f.init(k);
    	timing.aot_online.start();
    	BigInteger c = new BigInteger(1, f.compute(j_p.toByteArray()));
    	timing.aot_online.stop();
      // D sends c to C
    	//sanityCheck(C);
    	timing.aot_write.start();
      C.write(c);
      timing.aot_write.stop();
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("Error occured, not completing AOT, C will block");
    }
    
    C.bandwidth[PID.aot].stop();
	E.bandwidth[PID.aot].stop();
    C.countBandwidth = false;
    E.countBandwidth = false;
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
 // for testing
    /*
    switch (party) {
    case Charlie: // R
      int j = SR.rand.nextInt(10);
      System.out.println(j);
      System.out.println(AOT.executeC(con1, con2, j));
      break;
    case Debbie: // H
      AOT.executeD(con1, con2);
      break;
    case Eddie: // S
      String t = Util.addZero(new BigInteger(50, SR.rand).toString(2), 50);
      String[] m = new String[10];
      
      for (int i=0; i<10; i++)
        m[i] = t.substring(i*5, (i+1)*5);
      
      Util.printArrH(m);
      AOT.executeE(con1, con2, m);
      break;
    }
    
    System.out.println("Run completed");
    */
  }
}