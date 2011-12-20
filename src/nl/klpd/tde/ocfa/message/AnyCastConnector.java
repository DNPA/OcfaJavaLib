package nl.klpd.tde.ocfa.message;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * General mailbox which allows for sending and receiving messages.
 * @author joep
 * @codereview jochen
 */
public class AnyCastConnector {

	// the port on which the anycast is listening.
	private int port;
	// the host on which the anycast is listening.
	private String host;
	//private ModuleInstance address;
	private int startPort = 40000;
	// The object responsible for receiving messages.
	private IncomingMailBox mailReceiver;
	// The object responsible for sending messages. 
	private OutgoingMailBox mailSender;
	private ModuleInstance ownAddress;
	
	// The thread in which the incoming mailbox is listening for messages.
	private Thread incomingThread = null;
	private static Log logger = LogFactory.getLog(AnyCastConnector.class);
	
	// Internal messagefactory for parsing messages. 
	private DefaultMessageFactory factory;
	
	/**
	 * Constructor initializes some members.
	 * @param inHost host of anycast
	 * @param inPort post on which anycast is listening.
	 * @param inModuleName 
	 * @param inNameSpace
	 * @throws OcfaException
	 */
	public AnyCastConnector(String inHost, int inPort,  String inModuleName, String inNameSpace) 
		throws OcfaException{
		
		host = inHost;
		port = inPort;
		ownAddress = new ModuleInstance();
		ownAddress.setModuleName(inModuleName);
		ownAddress.setNamespace(inNameSpace);
		ownAddress.setInstance("java" + System.currentTimeMillis());
		try {
			ownAddress.setHost(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

			throw new OcfaException("cannot find own address: " + e1.getMessage());
		}

		try {
			factory= new DefaultMessageFactory();
			factory.setSender(ownAddress);
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		} catch (TransformerConfigurationException e) {

			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		}
		
	}
	/**
	 * connects to an anycast.
	 * @throws IOException
	 * @throws OcfaException
	 */
	public void connect() throws IOException, OcfaException{
		
		int port = createServerSocket();
		connectToAnycast(port);
	}

	/**
	 * creates a mailsender and sends a connect string to the anycast. telling it where it can connect.
	 * @param inPort the port on which the mailboxreceiver is listening.
	 * @throws IOException
	 * @throws OcfaException
	 * @TODO java modules can only connect to localhost.
	 */
	public void connectToAnycast(int inPort) throws IOException, OcfaException {

		mailSender = new OutgoingMailBox(host, port);
		mailSender.setMessageFactory(factory);
		mailSender.connect();
		String connectString =
			"<l2wrapper id=\"0\" type=\"internalconnect\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ " xsi:noNamespaceSchemaLocation=\"ocfa.xsd\">"
			+ " <message prio=\"0\">"
			+ " <sender host=\"" + ownAddress.getHost() + "\" instance=\"" + ownAddress.getInstance()
			+ "\" module=\"" + ownAddress.getModuleName() 
			+ "\" namespace=\"" + ownAddress.getNamespace() + "\"/> "
			+ " <broadcast/> "
			+ " <moduleinstance subject=\"" + inPort + "\"></moduleinstance> "
			+ "</message></l2wrapper>";
					
		mailSender.sendString(connectString);
	}

	/**
	 * Makes sure that the mailreceiver is running in a seperate thread and waiting for a connection from 
	 * the anycast.
	 * @return
	 * @throws IOException
	 * @throws OcfaException
	 */
	public int createServerSocket() throws IOException, OcfaException {
		
		mailReceiver = new IncomingMailBox(startPort);
		mailReceiver.setFactory(factory);
		incomingThread = new Thread(mailReceiver);
	
		incomingThread.start();
		try {
			while (mailReceiver.getPort() == 0){
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mailReceiver.getPort();
	}
	
	/**
	 * Get Next Message. Filters out message from the anycast and reacts to error messages by throwing an exception.
	 * @throws OcfaException 
	 */
	public Message getNextMessage() throws OcfaException{
		
		Message message  = null;
		while (message == null){
			
			try {
				message = mailReceiver.getNextMessage();
			
				switch(message.getContentType()){
				
				case MODULE_INSTANCE:
					this.ownAddress = message.getReceiver();
					incomingThread.setName(this.getOwnAddress().toString() + "-incoming");
					message = mailReceiver.getNextMessage();
					break;
				case ERROR:
					throw new OcfaException(OcfaException.Type.NO_CONNECTION_TO_ANYCAST, 
									message.getContent());
			
			} 
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new OcfaException(e.getMessage());
			}
		}
		return message;
		
	}

	/**
	 * Test program.
	 * @param argv
	 */
	public static  void main(String argv[]){
		
		String endPoint = "localhost";
		int port = 23111;
		try {
			AnyCastConnector connector = new AnyCastConnector(endPoint, port, "test", "java");
			connector.connect();
			System.out.println("waiting. press key to stop");

			System.in.read();
			System.out.println("key read");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OcfaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * sends a message that 'message is processed'
	 * @param message the message which has been processed.
	 * @throws OcfaException
	 */
	public void messageDone(Message message) throws OcfaException {
		
		if (message.getMessageType() == MessageType.TASK){
			
			Message sendMessage = mailReceiver.getFactory().createMessage();
			sendMessage.setMessageType(MessageType.TASK_PROGRESS);
			sendMessage.setCastType(CastType.UNICAST);
			sendMessage.setId(message.getId());
			// This part is totally unnecessary and only used in order to keep the anycast happy.
			sendMessage.setReceiver(message.getReceiver());
			sendMessage.setContentType(ContentType.EVIDENCE);
			sendMessage.setContent(message.getContent());
			sendMessage.setSubject("something");
			this.mailSender.sendMessage(sendMessage);
		}
		
	}
    /**
 	*  disconnects the anycastconnector
 	*/
	public void disconnect() {
		
		mailReceiver.setRunning(false);
		logger.info("interrupting thread");
		incomingThread.interrupt();
		while (incomingThread.getState() != Thread.State.TERMINATED){
			
			logger.info("waiting for incoming thread to end. State is: " + incomingThread.getState());
			try {
				
				Thread.currentThread().sleep(1000);
				mailReceiver.setRunning(false);

				incomingThread.interrupt();

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mailReceiver.getMessageQueue().size() > 0){
			
			logger.error("Still message in the messagequeue, stopping anyway");
		}
		mailSender.disconnect();
		
	}

	public ModuleInstance getOwnAddress() {
		return ownAddress;
	}

	public DefaultMessageFactory getFactory() {
		return factory;
	}

	public void setFactory(DefaultMessageFactory factory) {
		this.factory = factory;
	}


	public void sendMessage(Message inMessage) throws OcfaException{
		
		mailSender.sendMessage(inMessage);
	}

	public IncomingMailBox getMailReceiver() {
		return mailReceiver;
	}

	public OutgoingMailBox getMailSender() {
		return mailSender;
	}
	
}
