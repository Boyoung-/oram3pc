package sprout.ui;

import sprout.oram.Forest;

public class ForestTest
{
	public static void main(String[] args) throws Exception {
		Forest forest = new Forest();
		forest.buildFromFile("config/newConfig.yaml", "config/smallData.txt", "db.bin");		
	}

}
