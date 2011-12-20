package nl.klpd.tde.ocfa.module;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.evidence.EvidenceFactory;
import nl.klpd.tde.ocfa.evidence.dom.DomEvidenceFactory;
import nl.klpd.tde.ocfa.message.AnyCastConnector;
import nl.klpd.tde.ocfa.message.CastType;
import nl.klpd.tde.ocfa.message.Message;
import nl.klpd.tde.ocfa.message.MessageType;
import nl.klpd.tde.ocfa.message.ContentType;


import nl.klpd.tde.ocfa.misc.OcfaConfig;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.misc.OcfaModuleConfig;
import nl.klpd.tde.ocfa.store.DatabasePool;
import nl.klpd.tde.ocfa.store.OcfaRepository;
import nl.klpd.tde.ocfa.store.Repository;

/**
 * The OcfaModule runnable, launched by the OcfaModuleLauncher class
 * 
 * @author joep
 * @codereview jochen
**/

public class OcfaModule implements Runnable {

	private AnyCastConnector connector;
	private Repository repository;
	private boolean isRunning;
	private OcfaConfig config;
	private EvidenceFactory factory;
	private static Log logger = LogFactory.getLog(AnyCastConnector.class);

	public OcfaModule(String inName, String inNameSpace) throws OcfaException {
		try {
			config = new OcfaModuleConfig(null);
			String routerIp = config.getProperty("routerIP", "localhost");
			connector = new AnyCastConnector(routerIp, 23111, inName, inNameSpace);
			connector.connect();
			((OcfaModuleConfig)config).setMCurrentInstance(connector.getOwnAddress());
			setLogLevel();
			
			DatabasePool pool = new DatabasePool(config);
			repository = new OcfaRepository(config, pool);
			factory = new DomEvidenceFactory(connector.getOwnAddress());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		} 
	}
	
	private void setLogLevel() {
		String Log4jProperties;
		
		if ((Log4jProperties = config.getProperty("log4j")) != null){
			
			System.out.println("configuring with " + Log4jProperties);
			PropertyConfigurator.configure(Log4jProperties); 
		} else {
			
			BasicConfigurator.configure();
			String level = config.getProperty("syslog");
			
			/**
			 * Translate syslog levels to a org.apache.log4j.Level
			 * syslog 		| org.apache.log4j.Level
			 * ======		  ======================
			 * debug		| DEBUG 
			 * info			| INFO
			 * notice		| INFO
			 * warning		| WARN
			 * err			| ERROR
			 * crit			| FATAL
			 * alert		| INFO
			 * emerg		| FATAL
			 */
			if        (level.equals("err")){
				Logger.getRootLogger().setLevel(Level.ERROR);
			} else if (level.equals("alert")){
				Logger.getRootLogger().setLevel(Level.INFO);
			} else if (level.equals("emerg") || 
					   level.equals("crit")){
				Logger.getRootLogger().setLevel(Level.FATAL);
			} else if (level.equals("notice")) {
				Logger.getRootLogger().setLevel(Level.INFO);
			} else if (level.equals("warning")) {
				Logger.getRootLogger().setLevel(Level.WARN);
			} else if (level.equals("debug")) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
			} else if (level.equals("info")) {
				Logger.getRootLogger().setLevel(Level.INFO);
			} else {
				Logger.getRootLogger().setLevel(Level.toLevel(level,Level.INFO));
			}
		}
	}

	public void run(){
		
		Thread.currentThread().setName(this.getConnector().getOwnAddress().toString());
		isRunning = true;
		Message message = null;
		try {
			while (isRunning){
				message = connector.getNextMessage();
				try {
					processMessage(message);	
				} catch(OcfaException e){
					e.printStackTrace();
					logger.error(e.getMessage());
				}
			}
			logger.info("disconnecting stuff");
			connector.disconnect();
		
		} catch (OcfaException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			connector.disconnect();
		}
		
	}

	protected void processMessage(Message message) throws OcfaException {
		
		logger.info("Processing message " + message.getSubject());
		switch (message.getContentType()){
			case HALT:
				processHaltMessage(message);
				break;
			case EVIDENCE:
				processEvidenceMessage(message);
				break;
		}
		connector.messageDone(message);
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	protected void processEvidenceMessage(Message message) throws OcfaException {
		
		String metaReference = message.getContent();
		String evidenceString = repository.getMetaAsString(metaReference);
		logger.info("Creating evidence from: " + metaReference);
		Evidence evidence = factory.createEvidence(evidenceString);
		
		evidence.setMutable();
		logger.info("process Evidence");
		processEvidence(evidence);
		
		evidence.getActiveJob().close();
		
		logger.debug("Update evidence was " + factory.evidenceAsString(evidence));
		repository.updateMetaStoreEntity(metaReference, factory.evidenceAsString(evidence));
		
		logger.debug("creating messsage");
		
		/* Construct a new message to the ModuleType of the Instance the message was received from*/
		Message newMessage = connector.getFactory().createMessage();
		newMessage.setCastType(CastType.ANYCAST);
		newMessage.setReceiver(message.getSender().getModuleTypeOfInstance());
		newMessage.setContentType(ContentType.EVIDENCE);
		newMessage.setMessageType(MessageType.USER);
		newMessage.setContent(metaReference);
		newMessage.setPrio(message.getPrio());
		newMessage.setSubject("answer");
		logger.debug("sending message");
		connector.sendMessage(newMessage);
		
	}

	protected void processEvidence(Evidence evidence){
	}

	private void processHaltMessage(Message message) {
		logger.info("Halt received");
		isRunning = false;
	}
	
	public static void main(String argv []){
		
		OcfaModule module;
		try {
			System.out.println("connecting testmodule");
			
			module = new OcfaModule("testModule", "test");
			module.run();
			System.out.println("finished");

		} catch (OcfaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Repository getRepository() {
		return repository;
	}

	public EvidenceFactory getFactory() {
		return factory;
	}

	public OcfaConfig getConfig() {
		return config;
	}

	public AnyCastConnector getConnector() {
		return connector;
	}
	
}
