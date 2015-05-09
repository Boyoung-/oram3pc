package sprout.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import sprout.oram.ForestMetadata;
import sprout.oram.PID;
import sprout.util.Bandwidth;

public class CombineBandwidth {
	private static Bandwidth[] readFromFile(String filename) throws IOException {
		Bandwidth[] out = new Bandwidth[PID.size];

		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fin);
			for (int i = 0; i < PID.size; i++)
				out[i] = (Bandwidth) ois.readObject();

		} catch (ClassNotFoundException e1) {
			throw new IOException("File contains invalid structure.", e1);
		} finally {
			if (ois != null)
				ois.close();
		}

		return out;
	}

	private static Bandwidth[] add(Bandwidth[] a, Bandwidth[] b) {
		Bandwidth[] c = new Bandwidth[a.length];
		for (int i = 0; i < a.length; i++)
			c[i] = a[i].add(b[i]);
		return c;
	}

	public static void printVertical(Bandwidth[] a) {
		for (int i = 0; i < PID.size; i++) {
			System.out.println(a[i].bandwidth * 8);
		}
	}

	public static void printTSVFull(Bandwidth[] a) {
		for (int i = 0; i < PID.size; i++) {
			System.out.println(PID.names[i] + "\t" + a[i].bandwidth * 8);
		}
	}

	public static void main(String[] args) throws Exception {
		ForestMetadata.setup("config/newConfig.yaml", false);
		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();
		String suffix = "-t" + t + "n" + n + "w" + w + "d" + d;

		Bandwidth[] a;
		Bandwidth[] b;

		if (args.length == 1) {
			a = readFromFile("stats/" + args[0] + "-bandwidth-1" + suffix);
			b = readFromFile("stats/" + args[0] + "-bandwidth-2" + suffix);
			a = add(a, b);
			// printTSVFull(a);
			printVertical(a);
		} else if (args.length == 0) {
			a = readFromFile("stats/charlie-bandwidth-1" + suffix);
			b = readFromFile("stats/charlie-bandwidth-2" + suffix);
			a = add(a, b);
			b = readFromFile("stats/debbie-bandwidth-1" + suffix);
			a = add(a, b);
			b = readFromFile("stats/debbie-bandwidth-2" + suffix);
			a = add(a, b);
			b = readFromFile("stats/eddie-bandwidth-1" + suffix);
			a = add(a, b);
			b = readFromFile("stats/eddie-bandwidth-2" + suffix);
			a = add(a, b);
			printTSVFull(a);
		}
	}

}
