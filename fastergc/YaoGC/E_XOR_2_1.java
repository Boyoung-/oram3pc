// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

class E_XOR_2_1 extends XOR_2_1 {
    public E_XOR_2_1() {
	super();
    }

    protected void sendOutBitEncPair() {
    	if (outputWires[0].outBitEncPair != null) 
			outputWires[0].outBitEncPair = sender.readBigIntegerArray();
    }
}
