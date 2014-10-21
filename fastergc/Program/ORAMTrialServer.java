// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;
import java.util.*;
import java.security.SecureRandom;

import YaoGC.*;
import Utils.*;

public class ORAMTrialServer extends ProgServer {
    private BigInteger sBits;

    private State outputState;

    private BigInteger[][] sBitslps, cBitslps;

    private static final SecureRandom rnd = new SecureRandom();

    private String outputBits = null;

    public ORAMTrialServer(BigInteger bv, int length) {
	sBits = bv;
	ORAMTrialCommon.sBitLen = length;
    }

    protected void init() throws Exception {
	ORAMTrialCommon.cBitLen = ORAMTrialCommon.ois.readInt();
	ORAMTrialCommon.oos.writeInt(ORAMTrialCommon.sBitLen);
	ORAMTrialCommon.oos.flush();

	ORAMTrialCommon.initCircuits();

	generateLabelPairs();

	super.init();
    }

    private void generateLabelPairs() {
	sBitslps = new BigInteger[ORAMTrialCommon.sBitLen][2];
	cBitslps = new BigInteger[ORAMTrialCommon.cBitLen][2];

	for (int i = 0; i < ORAMTrialCommon.sBitLen; i++) {
	    BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
	    BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
	    sBitslps[i][0] = glb0;
	    sBitslps[i][1] = glb1;
	}

	for (int i = 0; i < ORAMTrialCommon.cBitLen; i++) {
	    BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
	    BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
	    cBitslps[i][0] = glb0;
	    cBitslps[i][1] = glb1;
	}
    }

    protected void execTransfer() throws Exception {
	for (int i = 0; i < ORAMTrialCommon.sBitLen; i++) {
	    int idx = sBits.testBit(i) ? 1 : 0;

	    int bytelength = (Wire.labelBitLength-1)/8 + 1;
	    Utils.writeBigInteger(sBitslps[i][idx], bytelength, ORAMTrialCommon.oos);
	}
	ORAMTrialCommon.oos.flush();
	StopWatch.taskTimeStamp("sending labels for selfs inputs");

	snder.execProtocol(cBitslps);
	StopWatch.taskTimeStamp("sending labels for peers inputs");
    }

    protected void execCircuit() throws Exception {
	BigInteger[] sBitslbs = new BigInteger[ORAMTrialCommon.sBitLen];
	BigInteger[] cBitslbs = new BigInteger[ORAMTrialCommon.cBitLen];

	for (int i = 0; i < sBitslps.length; i++)
	    sBitslbs[i] = sBitslps[i][0];

	for (int i = 0; i < cBitslps.length; i++)
	    cBitslbs[i] = cBitslps[i][0];

	outputState = ORAMTrialCommon.execCircuit(sBitslbs, cBitslbs);
    }

    protected void interpretResult() throws Exception {
	BigInteger[] outLabels = (BigInteger[]) ORAMTrialCommon.ois.readObject();

	BigInteger output = BigInteger.ZERO;
	for (int i = 0; i < outLabels.length; i++) {
	    if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
		if (outputState.wires[i].value == 1)
		    output = output.setBit(i);
		continue;
	    }
	    else if (outLabels[i].equals(outputState.wires[i].invd ? 
					 outputState.wires[i].lbl :
					 outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {
		    output = output.setBit(i);
	    }
	    else if (!outLabels[i].equals(outputState.wires[i].invd ? 
					  outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
					  outputState.wires[i].lbl)) 
		throw new Exception("Bad label encountered: i = " + i + "\t" +
				    outLabels[i] + " != (" + 
				    outputState.wires[i].lbl + ", " +
				    outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");
	}
	
	String outBits = output.toString(2);
	int totalLen = ORAMTrialCommon.sBitLen + ORAMTrialCommon.cBitLen;
	if (ORAMTrialCommon.circuit.equals("F2FT"))
	    totalLen = (totalLen - 2) / 2 + 2; 
	for (int i=outBits.length(); i<totalLen; i++)
	    outBits += "0";
	System.out.println("---- circuit output: " + outBits);
	outputBits = outBits;
	StopWatch.taskTimeStamp("output labels received and interpreted");
    }

    protected void verify_result() throws Exception {
	BigInteger cBits = (BigInteger) ORAMTrialCommon.ois.readObject();

	String cStr = cBits.toString(2);
	String sStr = sBits.toString(2);
	if (ORAMTrialCommon.cBitLen == 0)
	    cStr = "";
	if (ORAMTrialCommon.sBitLen == 0)
	    sStr = "";
	for (int i=cStr.length(); i<ORAMTrialCommon.cBitLen; i++)
	    cStr = "0" + cStr;
	for (int i=sStr.length(); i<ORAMTrialCommon.sBitLen; i++)
	    sStr = "0" + sStr;
	String inBits = cStr + sStr;
	inBits = new StringBuilder(inBits).reverse().toString();
	System.out.println("---- circuit  input: " + inBits);
	System.out.println();
    }

    protected void reset_input() throws Exception {
	SecureRandom sr = new SecureRandom();
	sBits = new BigInteger(ORAMTrialCommon.sBitLen, sr);
    }

    public String getOutput() {
	return outputBits;
    }
}