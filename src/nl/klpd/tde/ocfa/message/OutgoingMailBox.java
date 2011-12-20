package nl.klpd.tde.ocfa.message;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.misc.OcfaException.Type;
/**
 * Thing used for sending message to the anycast.
 * @author joep
 *
 */

public class OutgoingMailBox {

	private Socket socket;
	private String endPoint;
	private int endPort;
	private DefaultMessageFactory factory;
	private Logger logger = Logger.getLogger(this.getClass());
	public OutgoingMailBox(String inEndPoint,  int inEndPort){
		
		endPoint = inEndPoint;
		endPort = inEndPort;
	}

	public void connect() throws UnknownHostException, IOException{
		
		socket = new Socket(endPoint, endPort);
	}

	public void sendString(String inString) throws OcfaException {
		
		logger.info("sending " + inString);
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			writer.write(inString);
			writer.write('\n'); 
			writer.write(0x0A);
			writer.write(0x0);
			writer.flush();
		} catch(IOException e){
			
			throw new OcfaException(Type.NO_CONNECTION_TO_ANYCAST, e.getMessage());
		}
	}

	public void disconnect() {
		
		try {
			if (!socket.isClosed()){
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public DefaultMessageFactory getMessageFactory() {
		return factory;
	}

	public void setMessageFactory(DefaultMessageFactory factory) {
		this.factory = factory;
	}

	public void sendMessage(Message sendMessage) throws OcfaException {
		
		sendString(getMessageFactory().asPlainText(sendMessage));
		
	}



	
	
	
}
