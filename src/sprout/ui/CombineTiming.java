package sprout.ui;

import sprout.oram.ForestMetadata;
import sprout.util.Timing;

public class CombineTiming
{	
	public static void main(String[] args) throws Exception
	{
		ForestMetadata.setup("config/newConfig.yaml", false);
		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();
		String suffix = "-t" + t + "n" + n + "w" + w + "d" + d;
		
		Timing C = new Timing();
		Timing D = new Timing();
		Timing E = new Timing();
		
		C.readFromFile("stats/timing-charlie" + suffix);
		D.readFromFile("stats/timing-debbie" + suffix);
		E.readFromFile("stats/timing-eddie" + suffix);
		
		System.out.println(C.add(D).add(E).toCSV());

		//System.out.println(C);
		//System.out.println(D);
		//System.out.println(E);
	}

}
