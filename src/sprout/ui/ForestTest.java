package sprout.ui;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.TreeException;
import sprout.oram.TupleException;

public class ForestTest
{
	public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException, TupleException, TreeException, ForestException
	{
		ForestMetadata.setup("config/newConfig.yaml");
		Forest forest = new Forest();		
	}

}
