package nl.klpd.tde.ocfa.message;

import java.util.HashMap;
/**
 * enumeration of the different content types.
 * @author joep
 *
 */
public enum ContentType {

	EVIDENCE("evidence"),
	HALT("halt"),
	DISCONNECT("disconnect"),
	MODULE_INSTANCE("moduleinstance"),
	ERROR("error"),
	SYSTEM("system");
	
	private String type;
	private static HashMap<String, ContentType> contentTypes = 
		new HashMap<String, ContentType>();
	
	
	static {
		
		contentTypes.put("evidence", ContentType.EVIDENCE);
		contentTypes.put("halt", ContentType.HALT);
		contentTypes.put("disconnect", ContentType.DISCONNECT);
		contentTypes.put("moduleinstance", ContentType.MODULE_INSTANCE);
		contentTypes.put("error", ContentType.ERROR);
		contentTypes.put("system", ContentType.SYSTEM);
	}
	
	private ContentType(String inType){
		
		type = inType;
	}

	public static ContentType getContentType(String inContentType){
		
		return contentTypes.get(inContentType.toLowerCase());
	}

	public String getType() {
		return type;
	}
	
}
