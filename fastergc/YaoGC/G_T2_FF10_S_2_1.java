// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

class G_T2_FF10_S_2_1 extends T2_FF10_S_2_1 {
    public G_T2_FF10_S_2_1() {
	super();
    }

    protected void execYao() {
	fillTruthTable();
	encryptTruthTable();
	sendGTT();
	gtt = null;
    }
}
