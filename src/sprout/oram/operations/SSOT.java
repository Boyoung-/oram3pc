package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.TID;

// TODO: Possible parallelization opportunity in running each IOT
public class SSOT extends Operation {

	public SSOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public BigInteger[] executeC(Communication I, Communication E,
			BigInteger[] sC, int length) {
		IOT iot = new IOT(I, E);

		I.countBandwidth = false;
		E.countBandwidth = false;

		int l = length; // TODO: remove this?
		I.write(l);

		I.countBandwidth = true;
		E.countBandwidth = true;
		I.bandwidth[PID.ssot].start();
		E.bandwidth[PID.ssot].start();

		sanityCheck();

		// protocol
		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		BigInteger[] a = iot.executeR(I, E);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		iot.executeS(E, I, sC, length);

		I.countBandwidth = false;
		E.countBandwidth = false;
		I.bandwidth[PID.ssot].stop();
		E.bandwidth[PID.ssot].stop();

		// C outputs a
		return a;
	}

	public void executeI(Communication C, Communication E, Integer[] i)
			throws NoSuchAlgorithmException {
		IOT iot = new IOT(C, E);

		E.countBandwidth = false;
		C.countBandwidth = false;

		// parameters
		int k = i.length;
		int l = C.readInt(); // TODO: Can we make this an input

		C.countBandwidth = true;
		E.countBandwidth = true;
		C.bandwidth[PID.ssot].start();
		E.bandwidth[PID.ssot].start();

		sanityCheck();

		// protocol
		// step 1
		// party I
		timing.stopwatch[PID.ssot][TID.online].start();
		BigInteger[] delta = new BigInteger[k];
		for (int o = 0; o < k; o++)
			delta[o] = new BigInteger(l, SR.rand); // TODO: generate once?
		timing.stopwatch[PID.ssot][TID.online].stop();

		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		iot.executeI(C, E, i, delta);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		iot.executeI(E, C, i, delta);

		E.countBandwidth = false;
		C.countBandwidth = false;
		E.bandwidth[PID.ssot].stop();
		C.bandwidth[PID.ssot].stop();
	}

	public BigInteger[] executeE(Communication C, Communication I,
			BigInteger[] sE, int length) {
		IOT iot = new IOT(C, I);

		I.countBandwidth = true;
		C.countBandwidth = true;
		I.bandwidth[PID.ssot].start();
		C.bandwidth[PID.ssot].start();

		sanityCheck();

		// protocol
		// step 2
		// parties run IOT(E, C, I) on inputs sE for E and i, delta for I
		iot.executeS(C, I, sE, length);

		// step 3
		// parties run IOT(C, E, I) on inputs sC for C and i, delta for I
		BigInteger[] b = iot.executeR(I, C);

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
