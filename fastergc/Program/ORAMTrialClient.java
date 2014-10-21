// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>
// Modified by Boyang Wei to test ORAM circuits

package Program;

import java.math.*;
import java.security.SecureRandom;

import YaoGC.*;
import Utils.*;

public class ORAMTrialClient extends ProgClient {
    private BigInteger cBits;
    private BigInteger[] sBitslbs, cBitslbs;

    private State outputState;

    public ORAMTrialClient(BigInteger bv, int length) {
	cBits = bv;
	ORAMTrialCommon.cBitLen = length;
    }

    protected void init() throws Exception {
	ORAMTrialCommon.oos.writeInt(ORAMTrialCommon.cBitLen);
	ORAMTrialCommon.oos.flush();
    	ORAMTrialCommon.sBitLen = ORAMTrialCommon.ois.readInt();
	
	ORAMTrialCommon.initCircuits();

    	otNumOfPairs = ORAMTrialCommon.cBitLen;

    	super.init();
    }

    protected void execTransfer() throws Exception {
	sBitslbs = new BigInteger[ORAMTrialCommon.sBitLen];

	for (int i = 0; i < ORAMTrialCommon.sBitLen; i++) {
	    int bytelength = (Wire.labelBitLength-1)/8 + 1;
	    sBitslbs[i]   = Utils.readBigInteger(bytelength, ORAMTrialCommon.ois);
	}
	StopWatch.taskTimeStamp("receiving labels for peer's inputs");

	cBitslbs = new BigInteger[ORAMTrialCommon.cBitLen];
	rcver.execProtocol(cBits);
	cBitslbs = rcver.getData();
	StopWatch.taskTimeStamp("receiving labels for self's inputs");
    }

    protected void execCircuit() throws Exception {
	outputState = ORAMTrialCommon.execCircuit(sBitslbs, cBitslbs);
	System.out.println();
    }


    protected void interpretResult() throws Exception {
	ORAMTrialCommon.oos.writeObject(outputState.toLabels());
	ORAMTrialCommon.oos.flush();
    }

    protected void verify_result() throws Exception {
	ORAMTrialCommon.oos.writeObject(cBits);
	ORAMTrialCommon.oos.flush();
    }

    protected void reset_input() throws Exception {
	SecureRandom sr = new SecureRandom();
	cBits = new BigInteger(ORAMTrialCommon.cBitLen, sr);
    }
}