package sprout.oram.operations;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;

public class GCF extends Operation {
  public GCF(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public static void executeE(Communication C, Communication D) {
    System.out.println("Hello from E!");
  }
  
  public static void executeC(Communication D, Communication E) {
	  System.out.println("Hello from C!");
  }
  
  public static void executeD(Communication C, Communication E) {
	  System.out.println("Hello from D!");
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
 // for testing
    
    switch (party) {
    case Charlie:
      GCF.executeC(con1, con2);
      break;
    case Debbie:
      GCF.executeD(con1, con2);
      break;
    case Eddie:
      GCF.executeE(con1, con2);
      break;
    }
    
    System.out.println("Run completed");
    
  }
}