package sprout.ui;

import java.math.BigInteger;

import Program.*;

public class TestServer
{	
	public static void main(String[] args) throws Exception {
		ORAMTrialCommon.circuit = "F2ET";		
		BigInteger bits = new BigInteger("000000", 2);
		ORAMTrialServer oramtrialserver = new ORAMTrialServer(bits, 6);
		oramtrialserver.run();		
		oramtrialserver.getOutput();
		
		oramtrialserver = new ORAMTrialServer(bits, 6);
		oramtrialserver.run();		
		oramtrialserver.getOutput();
	}

}
