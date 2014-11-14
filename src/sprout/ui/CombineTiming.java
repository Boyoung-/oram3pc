package sprout.ui;

import sprout.util.Timing;

public class CombineTiming
{	
	public static void main(String[] args) throws Exception
	{
		Timing c = new Timing();
		Timing d = new Timing();
		Timing e = new Timing();
		
		c.readFromFile("stats/timing-charlie");
		d.readFromFile("stats/timing-debbie");
		e.readFromFile("stats/timing-eddie");
		
		System.out.println(c.add(d).add(e).toCSV());

		//System.out.println(c);
		//System.out.println(d);
		//System.out.println(e);
	}

}
