package sprout.crypto;

public class WrongPartyException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public WrongPartyException() {
    super();
  }

  public WrongPartyException(String message) {
    super(message);
  }

  public WrongPartyException(Throwable cause) {
    super(cause);
  }

  public WrongPartyException(String message, Throwable cause) {
    super(message, cause);
  }


}
