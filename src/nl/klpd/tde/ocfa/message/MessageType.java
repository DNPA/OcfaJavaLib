package nl.klpd.tde.ocfa.message;

import java.util.HashMap;

public enum MessageType {

	TASK("task"),
	USER("user"),
	TASK_PROGRESS("progress"),
	UNSUPPORTED("unsupported"),
	ERROR("error");
	private String typeName;
	private static HashMap<String, MessageType> messageTypes = new HashMap<String, MessageType>();
	
	static {
		
		messageTypes.put("task", TASK);
		messageTypes.put("user", USER);
		messageTypes.put("progress", TASK_PROGRESS);
		messageTypes.put("error", ERROR);
		
	}
	MessageType(String inString){
		
		typeName = inString;
	}

	public static MessageType getMessageType(String inString){
		
		MessageType type  = messageTypes.get(inString);
		if (type == null){
			
			type = UNSUPPORTED;
		}
		return type;
	}

	public String toString(){
		
		return typeName;
	}

}
