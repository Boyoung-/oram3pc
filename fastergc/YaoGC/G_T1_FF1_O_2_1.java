// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

class G_T1_FF1_O_2_1 extends T1_FF1_O_2_1 {
    public G_T1_FF1_O_2_1() {
	super();
    }

    protected void execYao() {
	fillTruthTable();
	encryptTruthTable();
	sendGTT();
	gtt = null;
    }
}
