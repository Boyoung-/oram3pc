// OUT OF DATE !!!!

// Run EvictionTest first for testing

package sprout.ui;

import java.math.BigInteger;

import Program.*;

public class EvictionTestClient
{
	
	public static void main(String[] args) throws Exception {
		ProgClient.serverIPname = "localhost";
		Program.iterCount = 1;
		ORAMTrialCommon.circuit = "F2ET";
		
		BigInteger bits = new BigInteger("0");
		ORAMTrialClient oramtrialclient = new ORAMTrialClient(bits, 0);
		oramtrialclient.run();
		
		Thread.sleep(4000);
		ORAMTrialCommon.circuit = "F2FT";
		oramtrialclient = new ORAMTrialClient(bits, 0);
		oramtrialclient.run();
	}

}
