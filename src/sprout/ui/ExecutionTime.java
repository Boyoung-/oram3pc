package sprout.ui;

import sprout.util.Timing;

public class ExecutionTime
{	
	public static void main(String[] args) throws Exception
	{
		Timing c = new Timing();
		Timing d = new Timing();
		Timing e = new Timing();
		
		c.readFromFile("files/timing-charlie");
		//System.out.println(c);
		d.readFromFile("files/timing-debbie");
		//System.out.println(d);
		e.readFromFile("files/timing-eddie");
		//System.out.println(e);
		
		//System.out.println(c.add(d).add(e));

		System.out.println(c);
		System.out.println(d);
		System.out.println(e);
	}

}
