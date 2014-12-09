package sprout.benchmarks;

import sprout.communication.Communication;
import sprout.oram.Party;

/**
 * This class implements the tests described here https://docs.google.com/a/uci.edu/document/d/1WFJvzA6Za-3fMO_0DCkWlWi732t-PEtI5YKwkOKHqx8/edit?usp=sharing
 *
 */
public class CommunicationBench {
  
  public static void Test1_A (Communication B) {
    
  }
  
  public static void Test1_B (Communication A) {
    
  }
  
  public static void sync(Communication a, Communication b) {
    a.write("synch");
    b.write("synch");
    // TODO: finish (just copy form earlier usage)
  }
  
  public static void TestAll(Communication a, Communication b, Party party) {
    
    // Branch on each party
    // Go through the tests
    // ensure synchronization
    
  }
}
