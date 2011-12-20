package nl.klpd.tde.ocfa.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.klpd.tde.ocfa.message.CastType;
import nl.klpd.tde.ocfa.message.ContentType;
import nl.klpd.tde.ocfa.message.Message;
import nl.klpd.tde.ocfa.message.MessageType;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.module.OcfaModule;

public class NudgeMonitorModule extends OcfaModule {

	private String status;
	private static Log log = LogFactory.getLog(MonitorModule.class);
	public NudgeMonitorModule()
	throws OcfaException {
		super("nudge", "default");
		// TODO Auto-generated constructor stub
	}


	public static void main(String argv[]){

		try {
			NudgeMonitorModule module = new NudgeMonitorModule();

			if (argv.length > 0){
				module.nudge(argv[0]);
				module.setRunning(false);

			} else {

				
				//module.run();
				System.out.println(module.askStatus());
			}

		} catch(OcfaException e){

			e.printStackTrace();
		}
	}


	private String askStatus() throws OcfaException {

		Message statusMessage = 
			getConnector().getMailSender().getMessageFactory().createMessage();
		ModuleInstance receiver = new ModuleInstance();
		receiver.setModuleName("monitor");
		receiver.setNamespace("default");
		statusMessage.setReceiver(receiver);
		statusMessage.setMessageType(MessageType.USER);
		statusMessage.setContentType(ContentType.SYSTEM);
		statusMessage.setSubject("status");
		statusMessage.setContent("");
		statusMessage.setCastType(CastType.ANYCAST);
		statusMessage.setSender(this.getConnector().getOwnAddress());
		getConnector().getMailSender().sendMessage(statusMessage);
		run();
		return getStatus();

	}


	private String getStatus() {
		// TODO Auto-generated method stub
		return status;
	}


	private void nudge(String inModule) throws OcfaException {

		Message nudgeMessage = 
			getConnector().getMailSender().getMessageFactory().createMessage();
		ModuleInstance receiver = new ModuleInstance();
		receiver.setModuleName("monitor");
		receiver.setNamespace("default");
		nudgeMessage.setReceiver(receiver);
		nudgeMessage.setMessageType(MessageType.USER);
		nudgeMessage.setContentType(ContentType.SYSTEM);
		nudgeMessage.setSubject("moduletypeadded");
		nudgeMessage.setContent(inModule);
		nudgeMessage.setCastType(CastType.ANYCAST);
		nudgeMessage.setSender(this.getConnector().getOwnAddress());

		getConnector().getMailSender().sendMessage(nudgeMessage);
	}


	public void processMessage(Message message) throws OcfaException{


		log.info("Processing message " + message.getSubject());
		switch (message.getContentType()){
		case SYSTEM:
			processSystemMessage(message);
			break;
		default:
			super.processMessage(message);
			return;
		}
		getConnector().messageDone(message);
	}


	private void processSystemMessage(Message message) {

		if (message.getSubject().equals("status")){

			status = message.getContent();
			setRunning(false);
		}

	}

}
