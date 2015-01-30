package sprout.ui;

import sprout.oram.ForestMetadata;
import sprout.util.Timing;

public class CombineTiming {
	public static void main(String[] args) throws Exception {
		ForestMetadata.setup("config/newConfig.yaml", false);
		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();
		String suffix = "-t" + t + "n" + n + "w" + w + "d" + d;

		Timing C = new Timing();
		Timing D = new Timing();
		Timing E = new Timing();

		if (args.length == 1) {
			if (args[0].equals("eddie")) {
				E.readFromFile("stats/timing-eddie" + suffix);
				System.out.println(E.toCSV() + "\n\n");
			}
			else if (args[0].equals("debbie")) {
				D.readFromFile("stats/timing-debbie" + suffix);
				System.out.println(D.toCSV() + "\n\n");
			}
			else {
				C.readFromFile("stats/timing-charlie" + suffix);
				System.out.println(C.toCSV() + "\n\n");
			}
		} else {
			C.readFromFile("stats/timing-charlie" + suffix);
			D.readFromFile("stats/timing-debbie" + suffix);
			E.readFromFile("stats/timing-eddie" + suffix);
			System.out.println(C.toCSV() + "\n\n");
			System.out.println(D.toCSV() + "\n\n");
			System.out.println(E.toCSV() + "\n\n");
		}

		// System.out.println(C.add(D).add(E).toCSV());
	}

}
