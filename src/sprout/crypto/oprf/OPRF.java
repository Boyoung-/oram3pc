package sprout.crypto.oprf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;

import sprout.crypto.CryptoException;
import sprout.crypto.SR;
import sprout.crypto.WrongPartyException;
import sprout.crypto.oprf.Message;
import sprout.util.Timing;

// For now we simply use an EC based OPRF. 
// Although, we may want to investigate Elgamal variants in the future. 
public class OPRF {
	public Timing timing;

	// Public keys
	protected ECPoint g, y;
	protected BigInteger n;

	// Private keys
	protected BigInteger k;

	/**
	 * Create a new keyed PRF
	 */
	public OPRF() {
		loadSharedParams();
		k = generateSecretKey();
	}

	/**
	 * Create a new PRF from saved keys
	 */
	public OPRF(BigInteger k, ECPoint y) {
		loadSharedParams();
		this.k = k;
		this.y = y;
	}

	/**
	 * Create a new PRF from a saved file
	 * 
	 * @param file
	 *            containing the keys
	 * @throws IOException
	 */
	public OPRF(String file) throws IOException {
		loadSharedParams();
		load(file);
	}

	/**
	 * @param y
	 *            public key of the secret holder
	 */
	public OPRF(ECPoint y) {
		loadSharedParams();
		this.y = y;
	}

	protected void loadSharedParams() {
		// TODO: Is this the curve we want to use?
		X9ECParameters x9 = NISTNamedCurves.getByName("P-224");
		g = x9.getG();
		n = x9.getN();
	}

	public ECPoint getG() {
		return g;
	}

	public BigInteger getN() {
		return n;
	}

	public BigInteger getK() {
		return k;
	}

	public ECPoint getY() {
		if (y == null) {
			y = g.multiply(k);
		}

		return y;
	}

	public ECPoint getPK() {
		return getY();
	}

	/**
	 * Is this the OPRF owner/server (with the key)
	 */
	public boolean hasKey() {
		return k != null;
	}

	/* **********************
	 * Save / Load ***********************
	 */

	public OPRF save(String filename) throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fout);
			oos.writeObject(k);
			oos.writeObject(y.getEncoded());
			return this;
		} finally {
			if (oos != null)
				oos.close();
		}
	}

	public OPRF load(String filename) throws IOException {
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
			k = (BigInteger) ois.readObject();
			byte[] yEnc = (byte[]) ois.readObject();
			y = NISTNamedCurves.getByName("P-224").getCurve().decodePoint(yEnc);

			return this;
		} catch (ClassNotFoundException e) {
			throw new IOException("File contains invalid structure.", e);
		} finally {
			if (ois != null)
				ois.close();
		}
	}

	/* **********************
	 * Protocol directives ***********************
	 */

	/**
	 * computes v = H(x)*g^t and w = y^-t
	 * 
	 * @param msg
	 *            input x
	 * @return Message containing v and w
	 * @throws WrongPartyException
	 *             If you already hold k
	 */
	public Message prepare(String msg) throws CryptoException,
			WrongPartyException {
		if (hasKey()) {
			throw new WrongPartyException(
					"Key holder cannot prepare messages, use evaluate instead");
		}

		return prepare(hash(msg));
	}

	public Message prepare(ECPoint msg) throws CryptoException,
			WrongPartyException {
		BigInteger t = randomRange(n);

		ECPoint gt = g.multiply(t);
		timing.oprf_online.start();
		ECPoint v = msg.add(gt);
		timing.oprf_online.stop();

		ECPoint w = y.multiply(t).negate();
		return new Message(v, w);
	}

	/**
	 * computes v' = v^k
	 * 
	 * @param msg
	 *            result of prepare
	 * @return v'
	 * @throws WrongPartyException
	 *             If you do not hold k
	 */
	//
	public Message evaluate(Message msg) throws CryptoException,
			WrongPartyException {
		if (!hasKey()) {
			throw new WrongPartyException("Only the key holder can evaluate");
		}
		return new Message(msg.getV().multiply(k));
	}

	/**
	 * Receiver computes v' * w
	 * 
	 * @param msg
	 *            result of evaluate and w from prepare
	 * @return H(x)^k
	 */
	public Message deblind(Message msg) {
		return new Message(msg.getV().add(msg.getW()));
	}

	/**
	 * compute v = H(msg)^k
	 * 
	 * @param msg
	 * @return v
	 * @throws WrongPartyException
	 *             If you do not hold k.
	 */
	public Message evaluate(String msg) throws CryptoException,
			WrongPartyException {
		if (!hasKey()) {
			throw new WrongPartyException("Only the key holder can evaluate");
		}

		return new Message(hash(msg).multiply(k));
	}

	/**
	 * Compute the PRF on msg
	 * 
	 * @param msg
	 * @return
	 */
	public Message evaluate(ECPoint msg) throws WrongPartyException {
		if (!hasKey()) {
			throw new WrongPartyException("Only the key holder can evaluate");
		}

		return new Message(msg.multiply(k));
	}

	/* **********************
	 * Utilities ***********************
	 */

	public ECPoint randomPoint() {
		return g.multiply(randomRange(n));
	}

	public BigInteger randomExponent() {
		return randomRange(n);
	}

	public BigInteger generateSecretKey() {
		return randomRange(n);
	}

	ECPoint hash(String input) throws CryptoException {
		return g.multiply(hash(input.getBytes(), (byte) 0).mod(n));
	}

	/**
	 * Compute SHA-1
	 * 
	 * @param message
	 * @param selector
	 *            Key to the hash. Useful for providing multiple unique hash
	 *            functions.
	 * @return H(selector | message)
	 * @throws CryptoException
	 *             if SHA-1 is not available
	 */
	static BigInteger hash(byte[] message, byte selector)
			throws CryptoException {

		// input = selector | message
		byte[] input = new byte[message.length + 1];
		System.arraycopy(message, 0, input, 1, message.length);
		input[0] = selector;

		MessageDigest digest = SR.digest;
		/*
		 * MessageDigest digest = null; try { digest =
		 * MessageDigest.getInstance("SHA-1"); } catch (NoSuchAlgorithmException
		 * e1) { throw new CryptoException("SHA-1 is not supported"); }
		 * digest.reset();
		 */

		return new BigInteger(digest.digest(input));
	}

	/**
	 * Calculate a random number between 0 and range (exclusive)
	 * 
	 * @param range
	 */
	static BigInteger randomRange(BigInteger range) {
		// TODO: Is there anything else we should fall back on here perhaps
		// openssl bn_range
		// another option is using an AES based key generator (the only
		// algorithim supported by android)

		// TODO: Should we be keeping this rand around?

		BigInteger temp = new BigInteger(range.bitLength(), SR.rand);
		while (temp.compareTo(range) >= 0 || temp.equals(BigInteger.ZERO)) {
			temp = new BigInteger(range.bitLength(), SR.rand);
		}
		return temp;

	}
}
