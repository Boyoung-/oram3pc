package sprout.oram.operations;

import java.math.BigInteger;

import Cipher.Cipher;
import YaoGC.State;
import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.PreData;
import sprout.oram.TID;

public class GCF extends Operation {
	public GCF(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeC(Communication D, Communication E, int n, BigInteger sC) {
		E.countBandwidth = true;
		D.countBandwidth = true;
		E.bandwidth[PID.gcf].start();
		D.bandwidth[PID.gcf].start();

		// sanityCheck();

		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		BigInteger[] K_C = new BigInteger[n];
		timing.stopwatch[PID.gcf][TID.online_read].start();
		for (int i = 0; i < n; i++) {
			A[i] = E.readBigIntegerArray();
		}
		timing.stopwatch[PID.gcf][TID.online_read].stop();

		timing.stopwatch[PID.gcf][TID.online].start();
		for (int i = 0; i < n; i++) {
			int beta = sC.testBit(n - i - 1) ? 1 : 0;
			K_C[i] = A[i][beta];
		}
		timing.stopwatch[PID.gcf][TID.online].stop();

		// step 2
		timing.stopwatch[PID.gcf][TID.online_write].start();
		D.write(K_C);
		timing.stopwatch[PID.gcf][TID.online_write].stop();

		E.countBandwidth = false;
		D.countBandwidth = false;
		E.bandwidth[PID.gcf].stop();
		D.bandwidth[PID.gcf].stop();
	}

	public BigInteger executeD(Communication C, Communication E,
			int tree_index, int level_index, int n) {
		C.countBandwidth = true;
		E.countBandwidth = true;
		C.bandwidth[PID.gcf].start();
		E.bandwidth[PID.gcf].start();

		// sanityCheck();

		// protocol
		// step 2
		timing.stopwatch[PID.gcf][TID.online_read].start();
		BigInteger[] K_C = C.readBigIntegerArray();
		timing.stopwatch[PID.gcf][TID.online_read].stop();

		// step 3
		timing.stopwatch[PID.gcf][TID.online].start();
		State in_D = State.fromLabels(K_C);
		PreData.gcf_gc_D[tree_index][level_index].startExecuting(in_D); 

		BigInteger output = BigInteger.ZERO;
		int length = PreData.gcf_gc_D[tree_index][level_index].outputWires.length;
		for (int i = 0; i < length; i++) {
			BigInteger lb = PreData.gcf_gc_D[tree_index][level_index].outputWires[i].lbl;
			int lsb = lb.testBit(0) ? 1 : 0;
			int k = PreData.gcf_gc_D[tree_index][level_index].outputWires[i].serialNum;
			int outBit = Cipher
					.decrypt(
							k,
							lb,
							PreData.gcf_gc_D[tree_index][level_index].outputWires[i].outBitEncPair[lsb]);
			if (outBit == 1)
				output = output.setBit(length - 1 - i);
		}
		timing.stopwatch[PID.gcf][TID.online].stop();

		C.countBandwidth = false;
		E.countBandwidth = false;
		C.bandwidth[PID.gcf].stop();
		E.bandwidth[PID.gcf].stop();

		return output;
	}

	public void executeE(Communication C, Communication D, int tree_index,
			int level_index, int n, BigInteger sE) {
		C.countBandwidth = true;
		D.countBandwidth = true;
		C.bandwidth[PID.gcf].start();
		D.bandwidth[PID.gcf].start();

		// sanityCheck();

		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		BigInteger[] K_E = new BigInteger[n];
		timing.stopwatch[PID.gcf][TID.online].start();
		for (int k = 0; k < n; k++) {
			int alpha = sE.testBit(n - k - 1) ? 1 : 0;
			A[k][0] = PreData.gcf_lbs[tree_index][level_index][k][alpha];
			A[k][1] = PreData.gcf_lbs[tree_index][level_index][k][1 - alpha];
			K_E[k] = PreData.gcf_lbs[tree_index][level_index][k][0];
		}
		timing.stopwatch[PID.gcf][TID.online].stop();

		timing.stopwatch[PID.gcf][TID.online_write].start();
		for (int i = 0; i < n; i++) {
			C.write(A[i]);
		}
		timing.stopwatch[PID.gcf][TID.online_write].stop();

		// step 3
		// in the Retrieval GC write/read will be
		// subtracted from this time
		timing.stopwatch[PID.gcf][TID.online].start();
		State in_E = State.fromLabels(K_E);
		PreData.gcf_gc_E[tree_index][level_index].startExecuting(in_E);
		timing.stopwatch[PID.gcf][TID.online].stop();

		C.countBandwidth = false;
		D.countBandwidth = false;
		C.bandwidth[PID.gcf].stop();
		D.bandwidth[PID.gcf].stop();
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}
}
