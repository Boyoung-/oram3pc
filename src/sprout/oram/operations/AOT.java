package sprout.oram.operations;

import java.math.BigInteger;

import sprout.communication.Communication;
import sprout.crypto.AES_PRF;
import sprout.crypto.SR;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.TID;

public class AOT extends Operation {
	public AOT(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeE(Communication C, Communication D, BigInteger[] m,
			int mLength) {
		C.countBandwidth = false;
		D.countBandwidth = false;

		int N = m.length;
		int l = mLength; // bits of each item in m

		// We may be able to do this without communication
		C.write(N);
		D.write(l);

		// pre-computed input
		// party E
		byte[] k = new byte[16];
		timing.stopwatch[PID.aot][TID.offline].start();
		SR.rand.nextBytes(k);
		timing.stopwatch[PID.aot][TID.offline].stop();
		// E sends k to D
		timing.stopwatch[PID.aot][TID.offline_write].start();
		D.write(k);
		timing.stopwatch[PID.aot][TID.offline_write].stop();

		C.countBandwidth = true;
		D.countBandwidth = true;
		C.bandwidth[PID.aot].start();
		D.bandwidth[PID.aot].start();

		// step 1
		// party E
		BigInteger[] m_p = new BigInteger[N];
		timing.stopwatch[PID.aot][TID.offline].start();
		AES_PRF f = null;
		try {
			f = new AES_PRF(l);
			f.init(k);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		timing.stopwatch[PID.aot][TID.offline].stop();

		sanityCheck();
		
		timing.stopwatch[PID.aot][TID.online].start();
		BigInteger alpha = BigInteger.valueOf(SR.rand.nextInt(N));
		try {
			for (int t = 0; t < N; t++) {
					m_p[t] = new BigInteger(1, f.compute(BigInteger.valueOf(t)
							.add(alpha).mod(BigInteger.valueOf(N)).toByteArray()))
							.xor(m[t]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timing.stopwatch[PID.aot][TID.online].stop();

		timing.stopwatch[PID.aot][TID.online_write].start();
		C.write(m_p);
		C.write(alpha);
		timing.stopwatch[PID.aot][TID.online_write].stop();
		// E sends m_p and alpha to C

		C.bandwidth[PID.aot].stop();
		D.bandwidth[PID.aot].stop();
		C.countBandwidth = false;
		D.countBandwidth = false;
	}

	public BigInteger executeC(Communication D, Communication E, int j) {
		D.countBandwidth = false;
		E.countBandwidth = false;

		int N = E.readInt();

		D.countBandwidth = true;
		E.countBandwidth = true;
		E.bandwidth[PID.aot].start();
		D.bandwidth[PID.aot].start();

		sanityCheck();

		// step 1
		// E sends m_p and alpha to C
		timing.stopwatch[PID.aot][TID.online_read].start();
		BigInteger[] m_p = E.readBigIntegerArray();
		BigInteger alpha = E.readBigInteger();
		timing.stopwatch[PID.aot][TID.online_read].stop();

		// step 2
		// party C
		timing.stopwatch[PID.aot][TID.online].start();
		BigInteger j_p = BigInteger.valueOf(j).add(alpha)
				.mod(BigInteger.valueOf(N));
		timing.stopwatch[PID.aot][TID.online].stop();

		// C sends j_p to D
		timing.stopwatch[PID.aot][TID.online_write].start();
		D.write(j_p);
		timing.stopwatch[PID.aot][TID.online_write].stop();

		// step 3
		// D sends c to C
		timing.stopwatch[PID.aot][TID.online_read].start();
		BigInteger c = D.readBigInteger();
		timing.stopwatch[PID.aot][TID.online_read].stop();

		timing.stopwatch[PID.aot][TID.online].start();
		BigInteger output = c.xor(m_p[j]);
		timing.stopwatch[PID.aot][TID.online].stop();
		// C outputs output

		D.bandwidth[PID.aot].stop();
		E.bandwidth[PID.aot].stop();
		D.countBandwidth = false;
		E.countBandwidth = false;

		return output;
	}

	public void executeD(Communication C, Communication E) {
		C.countBandwidth = false;
		E.countBandwidth = false;

		int l = E.readInt();

		// pre-computed input
		timing.stopwatch[PID.aot][TID.offline_read].start();
		byte[] k = E.read();
		timing.stopwatch[PID.aot][TID.offline_read].stop();

		C.countBandwidth = true;
		E.countBandwidth = true;
		C.bandwidth[PID.aot].start();
		E.bandwidth[PID.aot].start();

		sanityCheck();

		// protocol
		// step 2
		// C sends j_p to D
		timing.stopwatch[PID.aot][TID.online_read].start();
		BigInteger j_p = C.readBigInteger();
		timing.stopwatch[PID.aot][TID.online_read].stop();

		// step 3
		// party D
		try {
			timing.stopwatch[PID.aot][TID.offline].start();
			AES_PRF f = new AES_PRF(l);
			f.init(k);
			timing.stopwatch[PID.aot][TID.offline].stop();
			
			timing.stopwatch[PID.aot][TID.online].start();
			BigInteger c = new BigInteger(1, f.compute(j_p.toByteArray()));
			timing.stopwatch[PID.aot][TID.online].stop();
			// D sends c to C
			timing.stopwatch[PID.aot][TID.online_write].start();
			C.write(c);
			timing.stopwatch[PID.aot][TID.online_write].stop();
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("Error occured, not completing AOT, C will block");
		}

		C.bandwidth[PID.aot].stop();
		E.bandwidth[PID.aot].stop();
		C.countBandwidth = false;
		E.countBandwidth = false;
	}

	@Override
	public void run(Party party, Forest forest) throws ForestException {
	}
}
