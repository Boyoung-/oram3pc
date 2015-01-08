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
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.TID;

public class GCF extends Operation {
	public GCF(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeE(Communication C, Communication D, String circuit,
			int n, BigInteger sE) {
		C.countBandwidth = false;
		D.countBandwidth = false;

		// precomputation
		timing.stopwatch[PID.gcf][TID.offline].start();
		// setup circuit
		int w = n - 2;
		if (circuit.equals("F2FT"))
			w /= 2;
		int tmp1 = SR.rand.nextInt(w) + 1;
		int tmp2 = SR.rand.nextInt(w) + 1;
		int s1 = Math.min(tmp1, tmp2);
		int s2 = Math.max(tmp1, tmp2);

		Circuit gc_E = null;
		Circuit.isForGarbling = true;
		Circuit.timing = timing;
		if (circuit.equals("F2ET"))
			gc_E = new F2ET_Wplus2_Wplus2(w, s1, s2);
		else
			gc_E = new F2FT_2Wplus2_Wplus2(w, s1, s2);
		Circuit.setReceiver(D);
		try {
			gc_E.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < gc_E.outputWires.length; i++)
			// TODO: not a good way; should define a function
			gc_E.outputWires[i].outBitEncPair = new BigInteger[2];

		// generate label pairs
		BigInteger[][] lbs = new BigInteger[n][2];
		for (int i = 0; i < n; i++) {
			BigInteger glb0 = new BigInteger(Wire.labelBitLength, SR.rand);
			BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
			lbs[i][0] = glb0;
			lbs[i][1] = glb1;
		}
		timing.stopwatch[PID.gcf][TID.offline].stop();

		C.countBandwidth = true;
		D.countBandwidth = true;
		C.bandwidth[PID.gcf].start();
		D.bandwidth[PID.gcf].start();

		sanityCheck();

		// protocol
		// step 1
		BigInteger[][] A = new BigInteger[n][2];
		BigInteger[] K_E = new BigInteger[n];
		timing.stopwatch[PID.gcf][TID.online].start();
		for (int i = 0; i < n; i++) {
			int alpha = sE.testBit(n - i - 1) ? 1 : 0;
			A[i][0] = lbs[i][alpha];
			A[i][1] = lbs[i][1 - alpha];
			K_E[i] = lbs[i][0];
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
		gc_E.startExecuting(in_E);
		timing.stopwatch[PID.gcf][TID.online].stop();

		C.countBandwidth = false;
		D.countBandwidth = false;
		C.bandwidth[PID.gcf].stop();
		D.bandwidth[PID.gcf].stop();
	}

	public void executeC(Communication D, Communication E, int n, BigInteger sC) {
		E.countBandwidth = true;
		D.countBandwidth = true;
		E.bandwidth[PID.gcf].start();
		D.bandwidth[PID.gcf].start();

		sanityCheck();

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
			String circuit, int n) {
		C.countBandwidth = false;
		E.countBandwidth = false;

		// precomputation
		timing.stopwatch[PID.gcf][TID.offline].start();
		// setup circuit
		int w = n - 2;
		if (circuit.equals("F2FT"))
			w /= 2;

		Circuit gc_D = null;
		Circuit.isForGarbling = false;
		Circuit.timing = timing;
		if (circuit == "F2ET")
			gc_D = new F2ET_Wplus2_Wplus2(w, 1, 1);
		else
			gc_D = new F2FT_2Wplus2_Wplus2(w, 1, 1);
		Circuit.setSender(E);
		try {
			gc_D.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < gc_D.outputWires.length; i++)
			// TODO: not a good way; should define a function
			gc_D.outputWires[i].outBitEncPair = new BigInteger[2];
		timing.stopwatch[PID.gcf][TID.offline].stop();

		C.countBandwidth = true;
		E.countBandwidth = true;
		C.bandwidth[PID.gcf].start();
		E.bandwidth[PID.gcf].start();

		sanityCheck();

		// protocol
		// step 2
		timing.stopwatch[PID.gcf][TID.online_read].start();
		BigInteger[] K_C = C.readBigIntegerArray();
		timing.stopwatch[PID.gcf][TID.online_read].stop();

		// step 3
		timing.stopwatch[PID.gcf][TID.online].start();
		State in_D = State.fromLabels(K_C);
		gc_D.startExecuting(in_D); // TODO: separate gcf write/read time out of
									// this!

		BigInteger output = BigInteger.ZERO;
		int length = gc_D.outputWires.length;
		for (int i = 0; i < length; i++) {
			BigInteger lb = gc_D.outputWires[i].lbl;
			int lsb = lb.testBit(0) ? 1 : 0;
			int k = gc_D.outputWires[i].serialNum;
			int outBit = Cipher.decrypt(k, lb,
					gc_D.outputWires[i].outBitEncPair[lsb]);
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

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}
}
