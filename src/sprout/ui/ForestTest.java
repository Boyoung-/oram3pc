package sprout.ui;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.oram.TreeException;
import sprout.oram.Tuple;
import sprout.oram.TupleException;
import sprout.util.Util;

public class ForestTest
{
	public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException, TupleException, TreeException, ForestException
	{
		ForestMetadata.setup("config/newConfig.yaml");
		/*
		Forest forest = new Forest();		
		
		System.out.println();
		System.out.println("===== Testing get/set path =====");
		Tree t = forest.getTree(3);
		List<Tuple> p1 = t.getTuplesOnPath(1);
		t.setTuplesOnPath(p1, 3);
		List<Tuple> p2 = t.getTuplesOnPath(3);
		Util.printListV(p1);
		System.out.println();
		Util.printListV(p2);
		*/
	}

}
