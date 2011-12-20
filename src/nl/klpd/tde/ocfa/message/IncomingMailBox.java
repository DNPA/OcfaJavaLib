package nl.klpd.tde.ocfa.message;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * Class that waits for incoming messages.
 * @author joep
 *
 */
public class IncomingMailBox implements Runnable {

	private static Log logger = LogFactory.getLog(IncomingMailBox.class);

	private int startPort;
	private int port = 0;
	private Socket socket = null;
	private DefaultMessageFactory factory; 
	private boolean isRunning = true;
	// Queue that is used to cache messgae so that the anycastconnector can pick them up.
	private ArrayBlockingQueue<Message> messageQueue = new ArrayBlockingQueue<Message>(3);
	
	
	public IncomingMailBox(int inPort) throws OcfaException{
		
		startPort = inPort;
	
	}
	/**
	 * Method to be used in a separate thread. Waits until a connection is made. It then starts reading from 
	 * the connection until 0 is reached. It then parses the resulted message and puts it into the queue.
	 */
	public void run() {
		
		try {
			waitForConnection();

			BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());
			int c;
			StringBuffer messageBuffer = new StringBuffer("");			
	
			while (isRunning ){

				try {
					while (reader.available() == 0){
					
						Thread.sleep(400);
					}				
					c = reader.read();
					if (c == -1){
						
						logger.error("Inputstream socket has been closed");
					}
					if (c != 0){
						
						messageBuffer.append((char)c);
						
					} else {
						
						try {
							logger.info("getting " + messageBuffer.toString());
							Message message = factory.parseMessage(messageBuffer.toString());
							if (message.getMessageType() != MessageType.UNSUPPORTED){
								
								messageQueue.put(message);
							} else {
								
								logger.warn("dropping message " + messageBuffer.toString());
							}
							
							
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							
							logger.info("interrupted");
							if (!isRunning){
								
								logger.info("isRunning is false we should stop now.");
							} else {
								e.printStackTrace();
							}
						}
						messageBuffer = new StringBuffer();
					}
				} catch (InterruptedException e){
					 
					logger.info("sleep  of incoming thread interrupted");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				
				Message message = new DefaultMessage();
				message.setMessageType(MessageType.ERROR);
				message.setContentType(ContentType.ERROR);
				messageQueue.put(message);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			
			if (!socket.isClosed()){
				
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * Tries opeening a serversocket starting at startport. If it fails it will try a portnumber one higher 
	 * until the operation succeeds or still fails after 100 increases of the portnumber.
	 * @throws IOException
	 */
	public void waitForConnection() throws IOException {
	
		
		ServerSocket server = new ServerSocket();
		int port =  startPort;
		
		boolean connected = false;
		while( !connected){
		
			try {
				InetSocketAddress address = new InetSocketAddress(port);
			
				System.out.println("trying port " + port); 
				server.bind(address);
				
				connected = true;
				setPort(port); 
			} catch(BindException e){
				
				port++;
				if (port > startPort + 1000 ){
					
					throw new IOException("cannot find a port to listen on");
				}
			}
		}
		socket =  server.accept();
		server.close();
		//return socket;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	synchronized public int getPort() {
		return port;
	}
	synchronized public void setPort(int port) {
		this.port = port;
	}

	public Message getNextMessage() throws InterruptedException{
	
		logger.info("getNextMessage: amount of messages is " + messageQueue.size());
		return messageQueue.take();
	}
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	public ArrayBlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}
	public DefaultMessageFactory getFactory() {
		return factory;
	}
	public void setFactory(DefaultMessageFactory factory) {
		this.factory = factory;
	}
	
}
