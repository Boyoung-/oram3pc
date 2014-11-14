package sprout.ui;

import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.util.StopWatch;

public class InitForest
{	
	public static void main(String[] args) throws Exception
	{
		ForestMetadata.setup("config/newConfig.yaml");
		
		StopWatch sw = new StopWatch("Forest Initialization");
		sw.start();
		
		Forest forest = new Forest();
		//Forest forest = new Forest("files/forest.bin");
		
		sw.stop();
		System.out.println(sw);
	}

}
