package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import sprout.oram.Bucket;
import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.oram.Tuple;
import sprout.util.StopWatch;
import sprout.util.Util;

public class ForestTest
{
	static SecureRandom rnd = new SecureRandom();
	
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
