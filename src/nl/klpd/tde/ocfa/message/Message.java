package nl.klpd.tde.ocfa.message;
/**
 * Interface for an object that stores all features of a message.
 * @author joep
 *
 */
public interface Message {

	
	public CastType getCastType();
	public void setCastType(CastType castType);

	public abstract String getContent();
	public void setContent(String content);
	
	public abstract ModuleInstance getReceiver();
	public void setReceiver(ModuleInstance receiver) ;
	
	public abstract ModuleInstance getSender();
	public void setSender(ModuleInstance sender);
	public abstract String getSubject();
	public void setSubject(String subject);
	
	public abstract MessageType getMessageType();
	public void setMessageType(MessageType messageType);
	
	public abstract int getId();
	public void setId(int id);
	public abstract ContentType getContentType();
	public void setContentType(ContentType contentType);
	
	public void setPrio(int inPrio);
	public int getPrio();
}
