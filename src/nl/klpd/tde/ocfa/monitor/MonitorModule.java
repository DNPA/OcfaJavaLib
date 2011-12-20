package nl.klpd.tde.ocfa.monitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.klpd.tde.ocfa.message.CastType;
import nl.klpd.tde.ocfa.message.ContentType;
import nl.klpd.tde.ocfa.message.Message;
import nl.klpd.tde.ocfa.message.MessageType;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.module.OcfaModule;


/**
 * Module that registers on the monitor channel and checks for messages that a moduletype has been added
 * in the persistent queue. If the newly added module queueu is a java moduel it will start that appropriate module 
 * 
 * @author Joep
 *
 */
public class MonitorModule extends OcfaModule {

	private static Log log = LogFactory.getLog(MonitorModule.class);
	private Map<String, String> moduleNameClassMap = new HashMap<String, String>();
	private Map<String, Thread> nameModuleThreadMap= new HashMap<String, Thread>();
	private ClassLoader classLoader;
	
	
	public void loadLibraries(String [] inLibraries){

		String ocfaRoot;
		if (System.getenv("OCFAROOT") != null){
			ocfaRoot = System.getenv("OCFAROOT");
		} else {
			ocfaRoot = "/usr/local/digiwash/";
		}
		URL [] urls  = new URL[inLibraries.length];
		try {
			for (int x = 0; x < inLibraries.length; x++){
				urls[x]  =  new URL("file://" + ocfaRoot + "/lib/" + inLibraries[x]);
				log.info("adding library" + urls[x]);
			} 
		}
		catch (MalformedURLException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
	}
	
	
	public MonitorModule()
			throws OcfaException {
		super("monitor", "default");
		if (getConfig().getProperty("javalibraries")== null){
			
			log.warn("No java libraries found. Please check your configuration");
		} else {
			loadLibraries(getConfig().getProperty("javalibraries").split(","));
		}
		String javaModuleFile =  getConfig().getProperty("ocfaetc") + "/javamodules";
		try {
			BufferedReader reader = new BufferedReader( new FileReader(javaModuleFile));
			String line = reader.readLine();
			while (line != null){
				
				StringTokenizer tokenizer = new StringTokenizer(line, " ");
				String moduleName = tokenizer.nextToken();
				String className = tokenizer.nextToken();
				moduleNameClassMap.put(moduleName, className);
				line = reader.readLine();

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException("cannot find " + javaModuleFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException("cannot read from " + javaModuleFile);

		}
		
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


	protected void processSystemMessage(Message message) {
		
		if (message.getSubject().equals("moduletypeadded")){
			
			log.info("module added" + message.getContent());
			String moduleName = message.getContent();
			if (moduleNameClassMap.containsKey(moduleName) 
					&& !this.nameModuleThreadMap.containsKey(moduleName)){
				
				startModule(moduleName);
				
			}
		} else if (message.getSubject().equals("status")){
			
			
			try {
				giveStatus(message.getSender());
			} catch (OcfaException e) {
				// TODO Auto-generated catch block
				log.error("can't give an answer to a status request");
				e.printStackTrace();
			}
			
		}
		
	}


	private void giveStatus(ModuleInstance receiver) throws OcfaException {
		
		StringBuffer content = new StringBuffer("<modules>\n");
		for (Entry<String, Thread> nameModuleThreadEntry : nameModuleThreadMap.entrySet()){
			
			content.append("<module name=\"");
			content.append(nameModuleThreadEntry.getKey());
			content.append("\" \"value=\"");
			content.append(nameModuleThreadEntry.getValue().getState().toString());
			content.append("\">\n");
		}
		content.append("</modules>");
		Message answer = this.getConnector().getMailSender().getMessageFactory().createMessage();
		answer.setCastType(CastType.UNICAST);
		answer.setReceiver(receiver);
		answer.setSender(getConnector().getOwnAddress());
		answer.setContentType(ContentType.SYSTEM);
		answer.setMessageType(MessageType.USER);
		answer.setContent(content.toString());
		answer.setSubject("status");
		getConnector().sendMessage(answer);
	}


	@SuppressWarnings("unchecked")
	protected void startModule(String moduleName) {
		
		
		String className = this.moduleNameClassMap.get(moduleName);
		try {
			Class<OcfaModule> moduleClass = (Class<OcfaModule>) classLoader.loadClass(className);
		
			if (moduleClass == null){
		
				log.error(className + " not found in library");
			} else {
			
				Thread thread = new Thread(new OcfaRunnable(moduleClass));
				thread.setName(moduleName);
				nameModuleThreadMap.put(moduleName, thread);
				thread.start();			
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			log.error(className + " cannot be found.");
		}
	}

	
	public Map<String, String> getModuleNameClassMap() {
		return moduleNameClassMap;
	}


	public void setModuleNameClassMap(Map<String, String> moduleNameClassMap) {
		this.moduleNameClassMap = moduleNameClassMap;
	}

	protected void removeThread(String inModuleName){
		
		nameModuleThreadMap.remove(inModuleName);
	}

	protected class OcfaRunnable implements Runnable {

		private Class<OcfaModule> moduleClass;
		
		public OcfaRunnable(Class<OcfaModule> inModuleClass) {
			
			moduleClass= inModuleClass;
		}

		public void run() {
		
			long lastCrash = 0;
			int amountOfCrashes = 0;
			try {
				while (MonitorModule.this.isRunning() 
						&&  amountOfCrashes < 3){
									
					OcfaModule module = (OcfaModule) moduleClass.newInstance();
					Thread.currentThread().setName(module.getConnector().getOwnAddress().toString());
					try {
						module.run();
					} catch (Exception e){
						
						log.error(module.getConnector().getOwnAddress().toString() + ": " 
								+ e.getClass().getCanonicalName() + " thrown. Message: " + e.getMessage());
					}
						// assume module has crashed.
					if (MonitorModule.this.isRunning()){
					
						log.warn(module.getConnector().getOwnAddress().toString() + " seems to have crashed");
						// only count amount of crashes if less than 1 minute has passed since last crash.
						if (System.currentTimeMillis() - lastCrash > (60 * 1000)){
							
							log.warn("crash was long time ago. Set amount to 1");
							amountOfCrashes = 1;
						} else {
			
							amountOfCrashes++;
							log.warn("amount of crashes is: " + amountOfCrashes);

						}
						lastCrash = System.currentTimeMillis();

					}
					
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			} catch (InstantiationException e) {

				log.error("Instantation exception: " + e.getMessage());
				e.printStackTrace();
			} catch (IllegalAccessException e) {

				log.error("IllegalAccessException exception: " + e.getMessage());

				e.printStackTrace();
			} finally {
				
				log.info("removing: " + moduleClass.getCanonicalName());
				MonitorModule.this.removeThread(Thread.currentThread().getName());
			}
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String argv[]){
		
		MonitorModule module;
		try {
			module = new MonitorModule();

			if (argv.length > 0 && argv[0].equals("--nomon")){
				
				// start only one module.
				Class<OcfaModule> moduleClass = (Class<OcfaModule>) module.getClassLoader().loadClass(argv[1]);
				OcfaModule ocfaModule = moduleClass.newInstance();
				ocfaModule.run();
				
			} else {
				
			
				module.setRunning(true);
				for (String moduleName : argv){
				
					module.startModule(moduleName);
				}
				module.run();
			}
		} catch (OcfaException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
}
