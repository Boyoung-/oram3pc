package sprout.oram.operations;

import java.math.BigInteger;

import Cipher.Cipher;
import YaoGC.Circuit;
import YaoGC.F2ET_Wplus2_Wplus2;
import YaoGC.F2FT_2Wplus2_Wplus2;
import YaoGC.State;
import YaoGC.Wire;
import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.util.Util;

public class GCF extends Operation {
	public GCF(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeCharlie(Communication debbie, Communication eddie, int i, int level, int n, BigInteger sC_X) {
		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		BigInteger[] K_C = new BigInteger[n];
		for (int j = 0; j < n; j++) {
			A[j] = eddie.readBigIntegerArray();
		}

		for (int j = 0; j < n; j++) {
			int beta = sC_X.testBit(n - j - 1) ? 1 : 0;
			K_C[j] = A[j][beta];
		}

		// step 2
		debbie.write(K_C);
	}

	public BigInteger executeDebbie(Communication charlie, Communication eddie, int i, int level, int n) {
		// protocol
		// step 2
		BigInteger[] K_C = charlie.readBigIntegerArray();

		// step 3
		State in_D = State.fromLabels(K_C);
		PreData.gcf_gc_D[i][level].startExecuting(in_D); 

		BigInteger output = BigInteger.ZERO;
		int length = PreData.gcf_gc_D[i][level].outputWires.length;
		for (int j = 0; j < length; j++) {
			BigInteger lb = PreData.gcf_gc_D[i][level].outputWires[j].lbl;
			int lsb = lb.testBit(0) ? 1 : 0;
			int k = PreData.gcf_gc_D[i][level].outputWires[j].serialNum;
			int outBit = Cipher
					.decrypt(
							k,
							lb,
							PreData.gcf_gc_D[i][level].outputWires[j].outBitEncPair[lsb]);
			if (outBit == 1)
				output = output.setBit(length - 1 - j);
		}

		return output;
	}

	public void executeEddie(Communication charlie, Communication debbie, int i, int level, int n, BigInteger sE_X) {
		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		BigInteger[] K_E = new BigInteger[n];
		for (int k = 0; k < n; k++) {
			int alpha = sE_X.testBit(n - k - 1) ? 1 : 0;
			A[k][0] = PreData.gcf_lbs[i][level][k][alpha];
			A[k][1] = PreData.gcf_lbs[i][level][k][1 - alpha];
			K_E[k] = PreData.gcf_lbs[i][level][k][0];
		}

		for (int k = 0; k < n; k++) {
			charlie.write(A[k]);
		}

		// step 3
		// in the Retrieval GC write/read will be
		// subtracted from this time
		//State in_E = State.fromLabels(K_E);
		//PreData.gcf_gc_E[tree_index][level_index].startExecuting(in_E);
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing GCF  #####");

		int i = 0;
		int j = 0;
		int n = 10;
		int ww = 8;
		
		if (party == Party.Eddie) {
			PreData.gcf_gc_E = new Circuit[1][1];
			PreData.gcf_lbs = new BigInteger[1][1][n][2];

			int tmp1 = SR.rand.nextInt(ww) + 1;
			int tmp2 = SR.rand.nextInt(ww) + 1;
			int s1 = Math.min(tmp1, tmp2);
			int s2 = Math.max(tmp1, tmp2);
			Circuit.isForGarbling = true;
			Circuit.setReceiver(con2);
			PreData.gcf_gc_E[i][j] = new F2ET_Wplus2_Wplus2(ww, s1, s2);
			// PreData.gcf_gc_E[i][level] = new F2FT_2Wplus2_Wplus2(ww, s1, s2);
			try {
				PreData.gcf_gc_E[i][j].build();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int k = 0; k < PreData.gcf_gc_E[i][j].outputWires.length; k++)
				PreData.gcf_gc_E[i][j].outputWires[k].outBitEncPair = new BigInteger[2];

			BigInteger[] K_E = new BigInteger[n];
			for (int k = 0; k < n; k++) {
				BigInteger glb0 = new BigInteger(Wire.labelBitLength, SR.rand);
				BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
				PreData.gcf_lbs[i][j][k][0] = glb0;
				PreData.gcf_lbs[i][j][k][1] = glb1;
				K_E[k] = PreData.gcf_lbs[i][j][k][0];
			}

			State in_E = State.fromLabels(K_E);
			PreData.gcf_gc_E[i][j].sendTruthTables(in_E);
			
			BigInteger sE_X = new BigInteger(n-2, SR.rand).setBit(n-1).setBit(n-2);

			executeEddie(con1, con2, i, j, n, sE_X);
			
			BigInteger output = con2.readBigInteger();
			
			System.out.println("sigma: " + s1 + "  " + s2);
			System.out.println(Util.addZero(sE_X.toString(2), n));
			System.out.println(Util.addZero(output.toString(2), n));
			
		} else if (party == Party.Debbie) {
			PreData.gcf_gc_D = new Circuit[1][1];
			Circuit.isForGarbling = false;
			Circuit.setSender(con2);
			PreData.gcf_gc_D[i][j] = new F2ET_Wplus2_Wplus2(ww, 1, 1);
			// PreData.gcf_gc_D[i][j] = new F2FT_2Wplus2_Wplus2(ww, 1, 1);
			try {
				PreData.gcf_gc_D[i][j].build();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int k = 0; k < PreData.gcf_gc_D[i][j].outputWires.length; k++)
				PreData.gcf_gc_D[i][j].outputWires[k].outBitEncPair = new BigInteger[2];

			PreData.gcf_gc_D[i][j].receiveTruthTables();

			BigInteger output = executeDebbie(con1, con2, i, j, n);
			
			con2.write(output);

		} else if (party == Party.Charlie) {
			BigInteger sC_X = BigInteger.ZERO;
			
			executeCharlie(con1, con2, i, j, n, sC_X);
		}

		System.out.println("#####  Testing GCF Finished  #####");
	}
}
