package sprout.ui;

import java.io.FileNotFoundException;

import sprout.oram.Forest;
import sprout.oram.ForestMetadata;

public class ForestTest
{
	public static void main(String[] args) throws FileNotFoundException
	{
		ForestMetadata.setup("config/newConfig.yaml");
		//Forest forest = new Forest("config/smallData.txt");		
	}

}
