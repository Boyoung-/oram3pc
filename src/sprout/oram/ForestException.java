package sprout.oram;

public class ForestException extends Exception
{
	/**
	 * Default version ID.
	 */
	private static final long serialVersionUID = 1L;

	public ForestException(String msg)
	{
		super("FOREST EXCEPTION: " + msg);
	}
}
