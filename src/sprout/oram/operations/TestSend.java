package sprout.oram.operations;

import sprout.communication.Communication;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.util.StopWatch;

public class TestSend extends Operation {
	public TestSend(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeCharlie(Communication D, Communication E) {
		E.read();

		byte[][] a = new byte[1300 * 50][];
		for (int i = 0; i < 1300 * 50; i++) {
			;
			a[i] = E.read();
			// if ( i > 1)
			// a[i][0] = (byte) (a[i][0] ^ a[i-1][0]);
		}

	}

	public void executeDebbie(Communication C, Communication E) {

	}

	public void executeEddie(Communication C, Communication D, StopWatch sw1,
			StopWatch sw2) {
		byte[] bytes1 = new byte[1300 * 50];
		// SR.rand.nextBytes(bytes1);
		sw1.start();
		C.write(bytes1);
		sw1.stop();

		byte[][] bytes2 = new byte[1300 * 50][1];
		sw2.start();
		for (int i = 0; i < 1300 * 50; i++)
			C.write(bytes2[i]);
		sw2.stop();
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		if (party == Party.Eddie) {
			StopWatch sw1 = new StopWatch();
			StopWatch sw2 = new StopWatch();
			executeEddie(con1, con2, sw1, sw2);
			System.out.println(sw1.toTab());
			System.out.println(sw2.toTab());
		} else if (party == Party.Debbie) {
			executeDebbie(con1, con2);
		} else if (party == Party.Charlie) {
			executeCharlie(con1, con2);
		}
	}
}
