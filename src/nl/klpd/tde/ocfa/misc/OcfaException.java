package nl.klpd.tde.ocfa.misc;

public class OcfaException extends Exception {

	private Type type;
	public Type getType() {
		return type;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static enum Type {
		
		NO_CONNECTION_TO_DB,
		NO_CONNECTION_TO_ANYCAST,
		DATABASE_ERROR,
		READ_ERROR,
		UNKNOWN_ERROR
	}
	public OcfaException(String inMessage){
		this(Type.UNKNOWN_ERROR, inMessage);
		
	}
	
	public OcfaException(Type inType, String inMessage){
		super(inMessage);
		type = inType;
	}
}
