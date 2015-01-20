package sprout.oram.operations;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.oprf.Message;
import sprout.crypto.oprf.OPRF;
import sprout.oram.Bucket;
import sprout.oram.BucketException;
import sprout.oram.PID;
import sprout.oram.PreData;
import sprout.oram.TID;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.util.Util;

public class DecryptPath extends TreeOperation<DPOutput, BigInteger> {

	public DecryptPath(Communication con1, Communication con2) {
		super(con1, con2);
	}

	@Override
	public DPOutput executeCharlieSubTree(Communication debbie,
			Communication eddie, BigInteger Li) {
		debbie.countBandwidth = true;
		eddie.countBandwidth = true;
		debbie.bandwidth[PID.decrypt].start();
		eddie.bandwidth[PID.decrypt].start();

		//sanityCheck();

		// protocol
		// step 1
		// party C
		// C sends Li to E
		if (lBits > 0) {
			timing.stopwatch[PID.decrypt][TID.online_write].start();
			eddie.write(Li);
			timing.stopwatch[PID.decrypt][TID.online_write].stop();
		}

		// step 3
		// party C
		// E sends sigma_x to C
		timing.stopwatch[PID.decrypt][TID.online_read].start();
		ECPoint[] sigma_x = eddie.readECPointArray();
		timing.stopwatch[PID.decrypt][TID.online_read].stop();


		debbie.bandwidth[PID.oprf].start();
		eddie.bandwidth[PID.oprf].start();

		// step 4
		// party C and D run OPRF on C's input sigma_x and D's input k
		PRG G = new PRG(bucketBits);
		BigInteger[] secretC_P = new BigInteger[sigma_x.length];
		Message[] msg1 = new Message[sigma_x.length];
		Message[] msg2 = new Message[sigma_x.length];
		
		sanityCheck();
		
		timing.stopwatch[PID.oprf][TID.online].start();
		for (int j=0; j<sigma_x.length; j++)
			msg1[j] = new Message(sigma_x[j].add(PreData.oprf_gy[i][0][j]), PreData.oprf_gy[i][1][j]);
		timing.stopwatch[PID.oprf][TID.online].stop();

		timing.stopwatch[PID.oprf][TID.online_write].start();
		for (int j = 0; j < sigma_x.length; j++) 
			debbie.write(new Message(msg1[j].getV()));
		timing.stopwatch[PID.oprf][TID.online_write].stop();
		
		timing.stopwatch[PID.oprf][TID.online_read].start();
		for (int j = 0; j < sigma_x.length; j++) 
			msg2[j] = debbie.readMessage();
		timing.stopwatch[PID.oprf][TID.online_read].stop();
		
		timing.stopwatch[PID.oprf][TID.online].start();
		for (int j = 0; j < sigma_x.length; j++) {
			msg2[j].setW(msg1[j].getW());
			Message res = PreData.oprf_oprf.deblind(msg2[j]);
			secretC_P[j] = new BigInteger(1, G.compute(res.getResult()));
		}
		timing.stopwatch[PID.oprf][TID.online].stop();

		debbie.bandwidth[PID.oprf].stop();
		eddie.bandwidth[PID.oprf].stop();

		debbie.bandwidth[PID.decrypt].stop();
		eddie.bandwidth[PID.decrypt].stop();
		debbie.countBandwidth = false;
		eddie.countBandwidth = false;

		// C outputs secretC_P
		return new DPOutput(secretC_P, null, null);
	}

	@Override
	public DPOutput executeDebbieSubTree(Communication charlie,
			Communication eddie, BigInteger k) {
		charlie.countBandwidth = true;
		eddie.countBandwidth = true;
		charlie.bandwidth[PID.decrypt].start();
		eddie.bandwidth[PID.decrypt].start();

		//sanityCheck();

		// protocol
		// step 4
		timing.stopwatch[PID.oprf][TID.offline].start();
		OPRF oprf = OPRFHelper.getOPRF(false);
		timing.stopwatch[PID.oprf][TID.offline].stop();

		sanityCheck();

		charlie.bandwidth[PID.oprf].start();
		eddie.bandwidth[PID.oprf].start();
		
		Message[] msg = new Message[pathBuckets];
		
		timing.stopwatch[PID.oprf][TID.online_read].start();
		for (int j = 0; j < pathBuckets; j++) 
			msg[j] = charlie.readMessage();
		timing.stopwatch[PID.oprf][TID.online_read].stop();
		
		timing.stopwatch[PID.oprf][TID.online].start();
		for (int j = 0; j < pathBuckets; j++) 
			msg[j] = oprf.evaluate(msg[j]); // TODO: pass k as arg or just read from
										// file?
		timing.stopwatch[PID.oprf][TID.online].stop();
		
		timing.stopwatch[PID.oprf][TID.online_write].start();
		for (int j = 0; j < pathBuckets; j++) 
			charlie.write(msg[j]);
		timing.stopwatch[PID.oprf][TID.online_write].stop();

		charlie.bandwidth[PID.oprf].stop();
		eddie.bandwidth[PID.oprf].stop();

		eddie.bandwidth[PID.decrypt].stop();
		charlie.bandwidth[PID.decrypt].stop();
		charlie.countBandwidth = false;
		eddie.countBandwidth = false;

		// D outputs nothing
		return null;
	}

	@Override
	public DPOutput executeEddieSubTree(Communication charlie,
			Communication debbie, BigInteger arg_unused) {
		charlie.countBandwidth = true;
		debbie.countBandwidth = true;
		charlie.bandwidth[PID.decrypt].start();
		debbie.bandwidth[PID.decrypt].start();

		//sanityCheck();

		// protocol
		// step 1
		// party C
		// C sends Li to E
		BigInteger Li = null;

		if (lBits > 0) {
			timing.stopwatch[PID.decrypt][TID.online_read].start();
			Li = charlie.readBigInteger();
			timing.stopwatch[PID.decrypt][TID.online_read].stop();
		}

		// step 2
		// party E
		// E retrieves encrypted path Pbar using Li
		Bucket[] Pbar = null;
		try {
			timing.stopwatch[PID.decrypt][TID.online].start();
			Pbar = new Tree(i).getBucketsOnPath(Li);
			timing.stopwatch[PID.decrypt][TID.online].stop();
		} catch (TreeException e) {
			e.printStackTrace();
		} catch (BucketException e) {
			e.printStackTrace();
		}

		// step 3
		// party E
		// E sends sigma_x to C
		timing.stopwatch[PID.decrypt][TID.online].start();
		ECPoint[] x = new ECPoint[Pbar.length];
		BigInteger[] Bbar = new BigInteger[Pbar.length];
		for (int j = 0; j < Pbar.length; j++) {
			x[j] = Util.byteArrayToECPoint(Pbar[j].getNonce());
			Bbar[j] = new BigInteger(1, Pbar[j].getByteTuples());
		}
		ECPoint[] sigma_x = Util.permute(x, PreData.decrypt_sigma[i]);
		BigInteger[] secretE_P = Util.permute(Bbar, PreData.decrypt_sigma[i]);
		timing.stopwatch[PID.decrypt][TID.online].stop();

		timing.stopwatch[PID.decrypt][TID.online_write].start();
		charlie.write(sigma_x);
		timing.stopwatch[PID.decrypt][TID.online_write].stop();

		sanityCheck();

		debbie.bandwidth[PID.decrypt].stop();
		charlie.bandwidth[PID.decrypt].stop();
		charlie.countBandwidth = false;
		debbie.countBandwidth = false;

		// E outputs sigma and secretE_P
		return new DPOutput(null, secretE_P, PreData.decrypt_sigma[i]);
	}
}
