package sprout.oram;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import sprout.util.Util;

public class Forest {
	private static ArrayList<Tree> trees;

	private static ByteArray64 data1;
	private static ByteArray64 data2;

	private static String forestFile;
	private static boolean loadMemory = true;

	private void initTrees() {
		int levels = ForestMetadata.getLevels();
		trees = new ArrayList<Tree>();
		for (int i = 0; i < levels; i++)
			trees.add(new Tree(i));
	}

	public Forest(String mode) throws Exception {
		if (!ForestMetadata.getStatus())
			throw new ForestException("ForestMetadata is not setup");

		String[] defaultFilenames = ForestMetadata.getDefaultForestNames();

		if (mode.equals("init"))
			initForest(defaultFilenames[0], defaultFilenames[1]);
		else if (mode.equals("restore"))
			restoreForest(defaultFilenames[0]);
		else
			throw new ForestException("Unrecognized forest mode");
	}

	public Forest(String mode, String filename1, String filename2)
			throws Exception {
		if (!ForestMetadata.getStatus())
			throw new ForestException("ForestMetadata is not setup");

		if (mode.equals("init"))
			initForest(filename1, filename2);
		else if (mode.equals("restore"))
			restoreForest(filename1);
		else
			throw new ForestException("Unrecognized forest mode");
	}

	private void restoreForest(String filename) throws IOException {
		initTrees();
		if (loadMemory)
			readFromFile(filename);
		else
			forestFile = filename;
	}

	@SuppressWarnings("unchecked")
	private void initForest(String filename1, String filename2)
			throws Exception {
		loadMemory = true;
		data1 = new ByteArray64(ForestMetadata.getForestBytes());

		int levels = ForestMetadata.getLevels();
		int h = levels - 1;
		trees = new ArrayList<Tree>();
		for (int i = 0; i < levels; i++)
			trees.add(new Tree(i));

		long addressSpace = ForestMetadata.getAddressSpace();
		long numInsert = ForestMetadata.getNumInsert();
		if (numInsert < 0 || numInsert > addressSpace)
			numInsert = addressSpace;
		int tau = ForestMetadata.getTau();
		int w = ForestMetadata.getBucketDepth();
		int e = ForestMetadata.getLeafExpansion();
		// following used to hold new tuple content
		BigInteger FB = null;
		BigInteger[] N = new BigInteger[levels];
		BigInteger[] L = new BigInteger[levels];
		BigInteger A;
		BigInteger tuple; // new tuple to be inserted
		Bucket bucket; // bucket to be updated
		long bucketIndex; // bucket index in the tree
		int tupleIndex; // tuple index in the bucket

		HashMap<Long, Long>[] nToSlot = new HashMap[levels];
		for (int i = 1; i < levels; i++)
			nToSlot[i] = new HashMap<Long, Long>();

		int shiftN = ForestMetadata.getLastNBits() % tau;
		if (shiftN == 0)
			shiftN = tau;

		System.out.println("===== Forest Generation =====");
		for (long address = 0L; address < numInsert; address++) {
			System.out.println("record: " + address);
			for (int i = h; i >= 0; i--) {
				if (i == 0) {
					// FB = BigInteger.ONE;
					N[i] = BigInteger.ZERO;
					// L[i] = BigInteger.ZERO;
					bucketIndex = 0;
					tupleIndex = 0;
				} else {
					FB = BigInteger.ONE;
					if (i == h)
						N[i] = BigInteger.valueOf(address);
					else if (i == h - 1)
						N[i] = N[i + 1].shiftRight(shiftN);
					else
						N[i] = N[i + 1].shiftRight(tau);
					// N[i] = BigInteger.valueOf(address >> ((h-i)*tau));
					Long slot = nToSlot[i].get(N[i].longValue());
					if (slot == null) {
						do {
							slot = Util.nextLong(ForestMetadata
									.getNumLeafTuples(i));
						} while (nToSlot[i].containsValue(slot));
						nToSlot[i].put(N[i].longValue(), slot);
					}
					L[i] = BigInteger.valueOf(slot / (w * e));
					bucketIndex = slot / w + ForestMetadata.getNumLeaves(i) - 1;
					tupleIndex = (int) (slot % w);
				}

				bucket = trees.get(i).getBucket(bucketIndex);
				bucket.setIndex(i);

				if (i == h)
					// A = new BigInteger(ForestMetadata.getABits(i), rnd); //
					// generate random record content
					A = BigInteger.valueOf(address); // for testing: record
														// content is the same
														// as its N
				else {
					BigInteger indexN = null;
					if (i == h - 1)
						indexN = Util.getSubBits(N[i + 1], 0, shiftN);
					else
						indexN = Util.getSubBits(N[i + 1], 0, tau);
					int start = (ForestMetadata.getTwoTauPow()
							- indexN.intValue() - 1)
							* ForestMetadata.getLBits(i + 1);
					Tuple old = bucket.getTuple(tupleIndex);
					A = Util.setSubBits(new BigInteger(1, old.getA()),
							L[i + 1], start,
							start + ForestMetadata.getLBits(i + 1));
				}

				if (i == 0)
					tuple = A;
				else {
					tuple = FB
							.shiftLeft(ForestMetadata.getTupleBits(i) - 1)
							.or(N[i].shiftLeft(ForestMetadata.getLBits(i)
									+ ForestMetadata.getABits(i)))
							.or(L[i].shiftLeft(ForestMetadata.getABits(i)))
							.or(A);
				}

				Tuple newTuple = new Tuple(i, Util.rmSignBit(tuple
						.toByteArray()));
				bucket.setTuple(newTuple, tupleIndex);
				Util.disp("Tree-" + i + " writing " + newTuple);
				trees.get(i).setBucket(bucket, bucketIndex);
			}
			System.out.println("--------------------");
		}

		Util.disp("");

		// these two lines are real xors
		// data2 = new ByteArray64(ForestMetadata.getForestBytes(), "random");
		// data1.setXOR(data2);

		// this line is for testing
		data2 = new ByteArray64(ForestMetadata.getForestBytes(), "empty");

		writeToFile(filename1, filename2);
	}

	public void writeToFile(String filename1, String filename2)
			throws IOException {
		// File file = new File(filename);
		// FileUtils.writeByteArrayToFile(file, data);
		data1.writeToFile(filename1);
		if (filename2 != null)
			data2.writeToFile(filename2);
	}

	private void readFromFile(String filename) throws IOException {
		// File file = new File(filename);
		// data = FileUtils.readFileToByteArray(file);
		data1 = new ByteArray64(filename);
	}

	public Tree getTree(int index) throws ForestException {
		if (index < 0 || index >= trees.size()) {
			throw new ForestException("Invalid tree index: " + index);
		}
		return trees.get(index);
	}

	// TODO: make the following non-static?
	public static ByteArray64 getForestData() {
		return data1;
	}

	// TODO: thread-safe read/write??
	public static byte[] getForestData(long offset, int length) {
		if (loadMemory)
			return data1.getBytes(offset, length);

		byte[] content = new byte[length];
		try {
			RandomAccessFile raf = new RandomAccessFile(forestFile, "r");
			raf.seek(offset);
			raf.readFully(content, 0, length);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				throw new Exception("Forest.getForestData() error");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return content;
	}

	public static void setForestData(long offset, byte[] newData) {
		if (loadMemory) {
			data1.setBytes(offset, newData);
			return;
		}

		try {
			RandomAccessFile raf = new RandomAccessFile(forestFile, "rw");
			raf.seek(offset);
			raf.write(newData, 0, newData.length);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				throw new Exception("Forest.setForestData() error");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
