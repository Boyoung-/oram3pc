package sprout.crypto;

public class CryptoException extends RuntimeException
{
    /**
     * Default serial ID.
     */
    private static final long serialVersionUID = 1L;

    public CryptoException(String m)
    {
	super(m);
    }
	
    public CryptoException(Exception E)
    {
	super(E);
    }
}
