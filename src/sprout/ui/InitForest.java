package sprout.ui;

import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.util.StopWatch;

public class InitForest
{	
	public static void main(String[] args) throws Exception
	{
		ForestMetadata.setup("config/newConfig.yaml");
		int t = ForestMetadata.getTau();
		int n = ForestMetadata.getLastNBits();
		int w = ForestMetadata.getBucketDepth();
		int d = ForestMetadata.getDataSize();
		
		StopWatch sw = new StopWatch("Forest Initialization");
		sw.start();
		
		//Forest forest = new Forest(0, "files/forest_t" + t + "n" + n + "w" + w + "d" + d + "_r1000.bin");
		Forest forest = new Forest(0, "files/forest.bin");
		
		sw.stop();
		System.out.println(sw);
	}

}
