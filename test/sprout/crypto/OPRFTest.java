package sprout.crypto;

import java.io.IOException;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.Test;

import sprout.crypto.oprf.Message;
import sprout.crypto.oprf.OPRF;
import static org.junit.Assert.*;

public class OPRFTest {
  
  @Test
  public void testCorrectness() {
    // Select paramaters g and k | y = g^k
    OPRF serverOprf = new OPRF();
    OPRF clientOprf = new OPRF(serverOprf.getY());

    String x = "This is a test message";

    try {
      // Someone computes v = H(x)*g^t and w = y^-t
      Message msg = clientOprf.prepare(x);


      // NOTE: w is contained in msg, and for security should not be sent to debbie. 
      // Instead it should be held onto by charlie, and sent later to deblind. 
      // TODO: Remove the need to specify ECPoint here. 
      ECPoint w = msg.getW();

      // Owner computes v' = v^k
      msg = serverOprf.evaluate(new Message(msg.getV()));

      // Reciever computes v' * w
      msg = clientOprf.deblind(msg.setW(w));


      ECPoint expected = serverOprf.evaluate(x).getResult();
      assertTrue( expected.equals( msg.getResult() ));
    } catch (Exception exception) {
      System.out.println("Exception occured " + exception.getMessage());
    }
  }
  
  public static String test_file_dir = "test";
  @Test
  public void testSaveLoad() {
    OPRF serverOprf = new OPRF();
    OPRF clientOprf = new OPRF(serverOprf.getY());
   
    try {
      serverOprf.save(test_file_dir + "/" + "pub");
      clientOprf.save(test_file_dir + "/" + "priv");
      
      OPRF s2 = new OPRF(test_file_dir + "/" + "pub");
      OPRF c2 = new OPRF().load(test_file_dir+"/"+"priv");
      
      assertEquals(serverOprf.getK(), s2.getK());
      assertNull(clientOprf.getK());
      assertNull(c2.getK());
      
      assertEquals(serverOprf.getY(), s2.getY());
      assertEquals(clientOprf.getY(), c2.getY());
    } catch (IOException e) {
      e.printStackTrace();
      fail("IOException");
    }
    
  }
  
  @Test
  public void testParamaterSelection() {
    OPRF serverOprf = new OPRF();
    OPRF clientOprf = new OPRF();
    
    assertFalse(serverOprf.getK().equals( clientOprf.getK() ));

    assertTrue(serverOprf.getG().equals( clientOprf.getG() ));
    assertTrue(serverOprf.getN().equals( clientOprf.getN() ));
    
    clientOprf = new OPRF(serverOprf.getY());
    
    assertTrue(serverOprf.getY().equals( clientOprf.getY()) );
    assertTrue(serverOprf.getG().equals( clientOprf.getG() ));
    assertTrue(serverOprf.getN().equals( clientOprf.getN() ));
  }
}
