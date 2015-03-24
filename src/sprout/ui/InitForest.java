package sprout.ui;

import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.util.StopWatch;

public class InitForest {
	public static void main(String[] args) throws Exception {
		ForestMetadata.setup("config/newConfig.yaml");
		//String[] defaultFilenames = ForestMetadata.getDefaultForestNames();

		StopWatch sw = new StopWatch("Forest Setup");
		sw.start();
		
		new Forest("init");
		//new Forest("restore", defaultFilenames[0], null);
		//new Forest("restore", defaultFilenames[1], null);

		sw.stop();
		System.out.println(sw);
	}

}
