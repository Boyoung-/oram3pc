package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.util.Arrays;

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
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.util.Timing;
import sprout.util.Util;

public class GCF extends Operation {
	public GCF(Communication con1, Communication con2) {
		super(con1, con2);
	}

	public void executeCharlie(Communication debbie, Communication eddie,
			Timing localTiming, int i, int level, int n, BigInteger sC_X) {
		// protocol
		// step 1
		// localTiming.stopwatch[PID.gcf][TID.online_read].start();
		// byte[] msg_A = eddie.read();
		// localTiming.stopwatch[PID.gcf][TID.online_read].stop();

		localTiming.stopwatch[PID.gcf][TID.online].start();
		byte[] msg_K_C = new byte[n * Wire.labelBytes];

		for (int j = 0; j < n; j++) {
			int beta = sC_X.testBit(n - j - 1) ? 1 : 0;
			// System.arraycopy(msg_A, (beta*n+j)*Wire.labelBytes, msg_K_C,
			// j*Wire.labelBytes, Wire.labelBytes);
			msg_K_C[(j + 1) * Wire.labelBytes - 1] = (byte) beta;
		}
		localTiming.stopwatch[PID.gcf][TID.online].stop();

		// step 2
		localTiming.stopwatch[PID.gcf][TID.online_write].start();
		debbie.write(msg_K_C);
		localTiming.stopwatch[PID.gcf][TID.online_write].stop();
	}

	public BigInteger executeDebbie(Communication charlie, Communication eddie,
			Timing localTiming, int i, int level, int n) {
		// protocol
		// step 2
		localTiming.stopwatch[PID.gcf][TID.online_read].start();
		byte[] msg_A = eddie.read();

		byte[] msg_K_C = charlie.read();
		localTiming.stopwatch[PID.gcf][TID.online_read].stop();

		// step 3
		localTiming.stopwatch[PID.gcf][TID.online].start();
		BigInteger[] K_C = new BigInteger[n];
		for (int j = 0; j < n; j++) {
			// K_C[j] = new BigInteger(1, Arrays.copyOfRange(msg_K_C,
			// j*Wire.labelBytes, (j+1)*Wire.labelBytes));
			int beta = msg_K_C[(j + 1) * Wire.labelBytes - 1];
			K_C[j] = new BigInteger(1, Arrays.copyOfRange(msg_A, (beta * n + j)
					* Wire.labelBytes, (beta * n + j + 1) * Wire.labelBytes));
		}

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
			else if (outBit != 0) {
				System.out.println("**** GCF output error! ****");
			}
		}
		localTiming.stopwatch[PID.gcf][TID.online].stop();

		return output;
	}

	public void executeEddie(Communication charlie, Communication debbie,
			Timing localTiming, int i, int level, int n, BigInteger sE_X) {
		// protocol
		// step 1
		// TODO: put this BigInteger to byte[] conversion into precomputation
		// (wait for the protocol change)
		localTiming.stopwatch[PID.gcf][TID.online].start();
		byte[][][] A = new byte[n][2][];
		byte[] msg_A = new byte[Wire.labelBytes * n * 2];
		for (int k = 0; k < n; k++) {
			int alpha = sE_X.testBit(n - k - 1) ? 1 : 0;
			A[k][0] = PreData.gcf_lbs[i][level][k][alpha].toByteArray();
			A[k][1] = PreData.gcf_lbs[i][level][k][1 - alpha].toByteArray();
			if (A[k][0].length < Wire.labelBytes)
				System.arraycopy(A[k][0], 0, msg_A, (k + 1) * Wire.labelBytes
						- A[k][0].length, A[k][0].length);
			else
				System.arraycopy(A[k][0], A[k][0].length - Wire.labelBytes,
						msg_A, k * Wire.labelBytes, Wire.labelBytes);
			if (A[k][1].length < Wire.labelBytes)
				System.arraycopy(A[k][1], 0, msg_A, (n + k + 1)
						* Wire.labelBytes - A[k][1].length, A[k][1].length);
			else
				System.arraycopy(A[k][1], A[k][1].length - Wire.labelBytes,
						msg_A, (n + k) * Wire.labelBytes, Wire.labelBytes);
		}
		localTiming.stopwatch[PID.gcf][TID.online].stop();

		localTiming.stopwatch[PID.gcf][TID.online_write].start();
		// charlie.write(msg_A);
		debbie.write(msg_A);
		localTiming.stopwatch[PID.gcf][TID.online_write].stop();
	}

	// for testing correctness
	@Override
	public void run(Party party, Forest forest) throws ForestException {
		System.out.println("#####  Testing GCF  #####");

		timing = new Timing();

		boolean F2FT = false;

		int i = 0;
		int j = 0;
		int n = F2FT ? 18 : 10;
		int ww = 8;

		if (party == Party.Eddie) {
			PreData.gcf_gc_E = new Circuit[1][1];
			PreData.gcf_lbs = new BigInteger[1][1][n][2];

			int tmp1 = SR.rand.nextInt(ww);
			int tmp2 = SR.rand.nextInt(ww);
			int s1 = Math.min(tmp1, tmp2);
			int s2 = Math.max(tmp1, tmp2);
			Circuit.isForGarbling = true;
			Circuit.setReceiver(con2);
			PreData.gcf_gc_E[i][j] = F2FT ? new F2FT_2Wplus2_Wplus2(ww, s1, s2)
					: new F2ET_Wplus2_Wplus2(ww, s1, s2);
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

			BigInteger ones = BigInteger.ONE.shiftLeft(ww).subtract(
					BigInteger.ONE);
			BigInteger sE_X;
			if (F2FT)
				sE_X = new BigInteger(ww, SR.rand).shiftLeft(ww).xor(ones);
			else
				sE_X = new BigInteger(ww, SR.rand);

			executeEddie(con1, con2, timing, i, j, n, sE_X);

			BigInteger output = con2.readBigInteger();

			System.out.println("sigma: " + s1 + "  " + s2);
			System.out.println(Util.addZero(sE_X.toString(2), n));
			if (F2FT) {
				System.out.println(Util.addZero(
						sE_X.shiftRight(ww).toString(2), ww + 2));
			}
			System.out.println(Util.addZero(output.toString(2), ww + 2));

		} else if (party == Party.Debbie) {
			PreData.gcf_gc_D = new Circuit[1][1];
			Circuit.isForGarbling = false;
			Circuit.setSender(con2);
			PreData.gcf_gc_D[i][j] = F2FT ? new F2FT_2Wplus2_Wplus2(ww, 0, 0)
					: new F2ET_Wplus2_Wplus2(ww, 0, 0);
			try {
				PreData.gcf_gc_D[i][j].build();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int k = 0; k < PreData.gcf_gc_D[i][j].outputWires.length; k++)
				PreData.gcf_gc_D[i][j].outputWires[k].outBitEncPair = new BigInteger[2];

			PreData.gcf_gc_D[i][j].receiveTruthTables();

			BigInteger output = executeDebbie(con1, con2, timing, i, j, n);

			con2.write(output);

		} else if (party == Party.Charlie) {
			BigInteger sC_X = BigInteger.ZERO;

			executeCharlie(con1, con2, timing, i, j, n, sC_X);
		}

		System.out.println("#####  Testing GCF Finished  #####");
	}
}
