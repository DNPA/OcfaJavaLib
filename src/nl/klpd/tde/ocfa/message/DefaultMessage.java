package nl.klpd.tde.ocfa.message;
/**
 * 
 * Interface for an object that stores all features of a message.
 * @author joep
 * @codereview jochen
 */
public class DefaultMessage implements Message{

	private MessageType messageType;
	private ContentType contentType;
	private String  subject;
	private ModuleInstance sender;
	private ModuleInstance receiver;
	private String content;
	private CastType castType;
	private int id;
	private int prio;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public ModuleInstance getSender() {
		return sender;
	}
	public void setSender(ModuleInstance sender) {
		this.sender = sender;
	}
	
	public ModuleInstance getReceiver() {
		return receiver;
	}
	public void setReceiver(ModuleInstance receiver) {
		this.receiver = receiver;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public CastType getCastType() {
		return castType;
	}
	public void setCastType(CastType castType) {
		this.castType = castType;
	}
	
	public ContentType getContentType() {
		// TODO Auto-generated method stub
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public int getPrio() {
		return prio;
	}
	public void setPrio(int prio) {
		this.prio = prio;
	}
	
	
}
