package nl.klpd.tde.ocfa.message;
/**
 * Enum to differentiate between anycast, unicast and broadcast.
 * @author joep
 *
 */
public enum CastType {

	UNICAST("unicast"),
	ANYCAST("anycast"),
	BROADCAST("broadcast");
	
	private String typeName;
	CastType(String inString){
		
		typeName = inString;
	}
	
	public String toString(){
		
		return typeName;
	}
}
