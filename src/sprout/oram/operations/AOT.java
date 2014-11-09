package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.AES_PRF;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.util.Timing;
import sprout.util.Util;

public class AOT extends Operation {
  public AOT(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public static void executeE(Communication C, Communication D, String[] m) {
    int N = m.length;
    int l = m[0].length();
    
    // We may be able to do this without communication
    C.write(N);
    C.write(l);
    //D.write(N);
    D.write(l);
    
    // pre-computed input
    // party E
    byte[] k = new byte[16];
    rnd.nextBytes(k);
    // E sends k to D
    D.write(k);
    
    // step 1
    // party E
    Timing.aot_online.start();
    BigInteger alpha = BigInteger.valueOf(rnd.nextInt(N));
    Timing.aot_online.stop();
    BigInteger[] m_p = new BigInteger[N];
    try {
  	  AES_PRF f = new AES_PRF(l);
  	  f.init(k);
      Timing.aot_online.start();
      for (int t=0; t<N; t++) {
    	  m_p[t] = new BigInteger(1, f.compute(BigInteger.valueOf(t).add(alpha).mod(BigInteger.valueOf(N)).toByteArray())).xor(new BigInteger(m[t], 2));
      }
      Timing.aot_online.stop();
    } catch (Exception e){
      e.printStackTrace();
    }
    
    Timing.aot_write.start();
    C.write(m_p);
    C.write(alpha);
    Timing.aot_write.stop();
    // E sends m_p and alpha to C
    
  }
  
  public static String executeC(Communication D, Communication E, int j) {
    int N = E.readInt();
    int l = E.readInt();
    
    // step 1
    // E sends m_p and alpha to C
    Timing.aot_read.start();
    BigInteger [] m_p = E.readBigIntegerArray();
    BigInteger alpha = E.readBigInteger();
    Timing.aot_read.stop();
    
    // step 2
    //party C
    Timing.aot_online.start();
    BigInteger j_p = BigInteger.valueOf(j).add(alpha).mod(BigInteger.valueOf(N));
    Timing.aot_online.stop();
    // C sends j_p to D
    Timing.aot_write.start();
    D.write(j_p);
    Timing.aot_write.stop();
    
    // step 3
    // D sends c to C
    Timing.aot_read.start();
    BigInteger c = D.readBigInteger();
    Timing.aot_read.stop();
    
    Timing.aot_online.start();
    String output = Util.addZero(c.xor(m_p[j]).toString(2), l);
    Timing.aot_online.stop();
    // C outputs output
    
    return output;
  }
  
  public static void executeD(Communication C, Communication E) {
    //int N = E.readInt();
    int l = E.readInt();
    
    // pre-computed input
    byte[] k = E.read();
    
    // step 2
    // C sends j_p to D
    Timing.aot_read.start();
    BigInteger j_p = C.readBigInteger();
    Timing.aot_read.stop();
    
    // step 3
    // party D
    try {
    	AES_PRF f = new AES_PRF(l);
    	f.init(k);
    	Timing.aot_online.start();
    	BigInteger c = new BigInteger(1, f.compute(j_p.toByteArray()));
    	Timing.aot_online.stop();
      // D sends c to C
    	Timing.aot_write.start();
      C.write(c);
      Timing.aot_write.stop();
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("Error occured, not completing AOT, C will block");
    }
    
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
 // for testing
    
    switch (party) {
    case Charlie: // R
      int j = rnd.nextInt(10);
      System.out.println(j);
      System.out.println(AOT.executeC(con1, con2, j));
      break;
    case Debbie: // H
      AOT.executeD(con1, con2);
      break;
    case Eddie: // S
      String t = Util.addZero(new BigInteger(50, rnd).toString(2), 50);
      String[] m = new String[10];
      
      for (int i=0; i<10; i++)
        m[i] = t.substring(i*5, (i+1)*5);
      
      Util.printArrH(m);
      AOT.executeE(con1, con2, m);
      break;
    }
    
    System.out.println("Run completed");
    
  }
}