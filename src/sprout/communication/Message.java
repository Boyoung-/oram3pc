package sprout.communication;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class Message implements Serializable {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public Map<String, byte[]> arrayMap = new HashMap<String, byte[]>();
	public Map<String, Byte> scalarMap = new HashMap<String, Byte>();
	public Map<String, Integer> intMap = new HashMap<String, Integer>();
}
