package sprout.benchmarks;

import java.math.BigInteger;
import java.util.Random;

import sprout.communication.Communication;
import sprout.oram.Party;
import sprout.util.Bandwidth;
import sprout.util.StopWatch;

/**
 * This class implements the tests described here
 * https://docs.google.com/a/uci.edu
 * /document/d/1WFJvzA6Za-3fMO_0DCkWlWi732t-PEtI5YKwkOKHqx8/edit?usp=sharing
 *
 */
public class CommunicationBench {
	Communication a, b;

	static Random rand = new Random();

	private CommunicationBench(Communication a, Communication b) {
		this.a = a;
		this.b = b;
	}

	public static int ignored_trials = 1000;
	public static int num_trials = 1000;
	public static int N = 10;
	public static int bytes_per_object = 10000000;
	public static int wait_seconds = 1;

	// TODO: have a variant of Test1_A doesn't always send the same object
	public static void Test1_A(Communication B) {
		Bandwidth totalBand = new Bandwidth("totalWrite", false);
		StopWatch totalTime = new StopWatch("totalWrite", false);

		byte[] bytes = new byte[bytes_per_object];
		// Ensure all bytes are non-null
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 0x3B;
		}
		// could generate random bytes using
		// rand.nextBytes(bytes) but this does not have consistent length.

		// String obj = new String(bytes);
		// byte[] obj = bytes;
		BigInteger obj = new BigInteger(bytes);

		for (int i = 0; i < num_trials + ignored_trials; i++) {
			sync(B, B);
			StopWatch sw = new StopWatch();
			sw.start();
			B.sharedBandwidth.clear();
			//B.sharedBandwidth.start();

			for (int j = 0; j < N; j++) {
				B.write(obj);
			}

			//B.sharedBandwidth.stop();
			sw.stop();

			if (i >= ignored_trials) {
				totalBand.add_mut(B.sharedBandwidth);
				totalTime.add_mut(sw);
			}
		}

		totalBand.divide(num_trials);
		totalTime.divide(num_trials);
		log("-------- Performance of STRINGS ---------");
		log("Data transfer: " + N * bytes_per_object);
		log("A: average total bandwidth: " + totalBand.toString());
		log("A: average total time: " + totalTime.toString());
		log("-----------------------------------------");
	}

	public static void Test1_B(Communication A) {
		Test1_B(A, 0);
	}

	public static void Test1_B(Communication A, long read_delay) {
		Bandwidth totalBand = new Bandwidth("totalWrite", false);
		StopWatch totalTime = new StopWatch("totalWrite", false);

		if (read_delay != 0) {
			log("Waiting for " + read_delay / 1000.0 + " seconds");
		}

		for (int i = 0; i < num_trials + ignored_trials; i++) {
			StopWatch sw = new StopWatch();
			sync(A, A);
			//A.sharedBandwidth.start();
			A.sharedBandwidth.clear();

			if (read_delay != 0) {
				try {
					Thread.sleep(read_delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			sw.start();

			for (int j = 0; j < N; j++) {
				A.readBigInteger();
				// A.read();
				// A.readString();
			}

			//A.sharedBandwidth.stop();
			sw.stop();

			if (i >= ignored_trials) {
				totalBand.add_mut(A.sharedBandwidth);
				totalTime.add_mut(sw);
			}
		}

		totalBand.divide(num_trials);
		totalTime.divide(num_trials);
		log("-------- Performance of STRINGS ---------");
		log("Data transfer: " + N * bytes_per_object);
		log("B: average total bandwidth: " + totalBand.toString());
		log("B: average total time: " + totalTime.toString());
		log("-----------------------------------------");
	}

	public static void Test2_A(Communication B) {
		Test1_A(B);
	}

	public static void Test2_B(Communication A) {
		Test1_B(A, wait_seconds * 1000);
	}

	public static void Test3_A(Communication B) {
		Bandwidth totalBand = new Bandwidth("totalWrite", false);
		StopWatch totalTime = new StopWatch("totalWrite", false);

		byte[] bytes = new byte[bytes_per_object];
		// Ensure all bytes are non-null
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = 0x3B;
		}
		// could generate random bytes using
		// rand.nextBytes(bytes) but this does not have consistent length.

		// String obj = new String(bytes);
		byte[] obj = bytes;
		// BigInteger obj = new BigInteger(bytes);

		for (int i = 0; i < num_trials + ignored_trials; i++) {
			System.out.println("i= " + i);
			sync(B, B);
			
			B.sharedBandwidth.clear();
			StopWatch sw = new StopWatch();
			
			sw.start();
			B.write(obj);
			B.read();
			sw.stop();
			
			B.sharedBandwidth.add(obj.length);

			if (i >= ignored_trials) {
				totalBand.add_mut(B.sharedBandwidth);
				totalTime.add_mut(sw);
			}
		}

		totalBand.divide(num_trials);
		totalTime.divide(num_trials);
		log("-------- Performance of STRINGS ---------");
		log("Data transfer: " + bytes_per_object);
		log("A: average total bandwidth: " + totalBand.toString());
		log("A: average total time: " + totalTime.toString());
		log("-----------------------------------------");
	}

	public static void Test3_B(Communication A) {
		Bandwidth totalBand = new Bandwidth("totalWrite", false);
		StopWatch totalTime = new StopWatch("totalWrite", false);

		for (int i = 0; i < num_trials + ignored_trials; i++) {
			StopWatch sw = new StopWatch();
			sync(A, A);
			A.sharedBandwidth.clear();
			
			sw.start();
			byte[] tmp = A.read();
			A.write(tmp);
			sw.stop();
			
			A.sharedBandwidth.add(tmp.length);

			if (i >= ignored_trials) {
				totalBand.add_mut(A.sharedBandwidth);
				totalTime.add_mut(sw);
			}
		}

		totalBand.divide(num_trials);
		totalTime.divide(num_trials);
		log("-------- Performance of Test3B ----------");
		log("Data transfer: " + bytes_per_object);
		log("B: average total bandwidth: " + totalBand.toString());
		log("B: average total time: " + totalTime.toString());
		log("-----------------------------------------");
	}

	void sync() {
		sync(a, b);
	}

	public static void sync(Communication a, Communication b) {
		a.write("synch");
		b.write("synch");
		String s;
		if (!(s = a.readString()).equals("synch")) {
			log("Synch failed for a. Got '" + s + "'");
			throw new RuntimeException("Synchronization Failed");
		}
		if (!(s = b.readString()).equals("synch")) {
			log("Synch failed for b. Got '" + s + "'");
			throw new RuntimeException("Synchronization Failed");
		}
	}

	public static void log(String s) {
		System.out.println(s);
	}

	public static void TestAll(Communication a, Communication b, Party party) {
		new CommunicationBench(a, b).TestAll(party);
	}

	public void TestAll(Party party) {
		//a.countBandwidth = true;
		//b.countBandwidth = true;

		// Strange way to start bandwidth monitoring but oh well.
		// Would be better to have a method on Com object.
		// a.sharedBandwidth.start();

		// Test 1
		log("Before Test 1");
		sync();
		if (party.equals(Party.Charlie)) {
			// Test1_A(a);
		} else if (party.equals(Party.Debbie)) {
			// Test1_B(a);
		}
		log("Before Test 2");
		sync();
		if (party.equals(Party.Charlie)) {
			// Test2_A(a);
		} else if (party.equals(Party.Debbie)) {
			// Test2_B(a);
		}
		sync();
		log("before Test 3");
		if (party.equals(Party.Charlie)) {
			Test3_A(a);
		} else if (party.equals(Party.Debbie)) {
			Test3_B(a);
		}
		sync();
	}
}
