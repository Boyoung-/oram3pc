package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.TID;
import sprout.util.Timing;

// TODO: Possible parallelization opportunity in running each IOT
public class SSOT extends Operation {

	public SSOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeC(Communication I, Communication E, Timing localTiming,
			BigInteger[] sC, int index) {
		IOT iot = new IOT(I, E);

		I.countBandwidth = true;
		E.countBandwidth = true;
		I.bandwidth[PID.ssot].start();
		E.bandwidth[PID.ssot].start();

		// protocol
		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		BigInteger[] a = iot.executeR(I, E, localTiming, index);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		iot.executeS(E, I, localTiming, sC, index);

		I.countBandwidth = false;
		E.countBandwidth = false;
		I.bandwidth[PID.ssot].stop();
		E.bandwidth[PID.ssot].stop();

		// C outputs a
		return a;
	}

	public void executeI(Communication C, Communication E, Timing localTiming, Integer[] i,
			int index) throws NoSuchAlgorithmException {
		IOT iot = new IOT(C, E);

		E.countBandwidth = false;
		C.countBandwidth = false;

		// parameters
		int k = i.length;
		int l = ForestMetadata.getTupleBits(index);

		C.countBandwidth = true;
		E.countBandwidth = true;
		C.bandwidth[PID.ssot].start();
		E.bandwidth[PID.ssot].start();

		// protocol
		// step 1
		// party I
		localTiming.stopwatch[PID.ssot][TID.online].start();
		BigInteger[] delta = new BigInteger[k];
		for (int o = 0; o < k; o++)
			delta[o] = new BigInteger(l, SR.rand); // TODO: generate once?
		localTiming.stopwatch[PID.ssot][TID.online].stop();

		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		iot.executeI(C, E, localTiming, i, delta, index, 0);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		iot.executeI(E, C, localTiming, i, delta, index, 1);

		E.countBandwidth = false;
		C.countBandwidth = false;
		E.bandwidth[PID.ssot].stop();
		C.bandwidth[PID.ssot].stop();
	}

	public BigInteger[] executeE(Communication C, Communication I, Timing localTiming,
			BigInteger[] sE, int index) {
		IOT iot = new IOT(C, I);

		I.countBandwidth = true;
		C.countBandwidth = true;
		I.bandwidth[PID.ssot].start();
		C.bandwidth[PID.ssot].start();

		// protocol
		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		iot.executeS(C, I, localTiming, sE, index);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		BigInteger[] b = iot.executeR(I, C, localTiming, index);

		I.countBandwidth = false;
		C.countBandwidth = false;
		I.bandwidth[PID.ssot].stop();
		C.bandwidth[PID.ssot].stop();

		// E outputs b
		return b;
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}

}
