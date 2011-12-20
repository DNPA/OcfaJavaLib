package nl.klpd.tde.ocfa.module;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaConfig;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.misc.OcfaModuleConfig;

import org.apache.commons.logging.*;
/**
 * Utility for launching modules. loads all libraries in the configuration file stored in javalibraries
 * then launches all modules stored in 'javamodules' in separate threads.
 * @author joep
 * @codereview jochen
 */
public class OcfaModuleLauncher {

	static private Log logger= LogFactory.getLog("OcfaModuleLauncher");
	
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
				logger.info("adding library" + urls[x]);
			} 
		}
		catch (MalformedURLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
	}
	
	
	@SuppressWarnings("unchecked")
	public void startModules(String [] inModuleNames){
		
		for (String javaModule : inModuleNames){
			
			logger.info("trying module" + javaModule);

			if (javaModule.length() > 0){
				
				logger.info("trying module" + javaModule);
				Class<OcfaModule> moduleClass;
				try {
					
					moduleClass = (Class<OcfaModule>) classLoader.loadClass(javaModule);
			
					if (moduleClass == null){
					
						logger.error(javaModule + " not an ocfamodule");
					} else {
						
						OcfaModule module = moduleClass.newInstance();
						Thread thread = new Thread(module);
						thread.setName(module.getConnector().getOwnAddress().toString());
						thread.start();			
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(javaModule + " cannot be found.");
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}		
	}
	
	public static void main(String argv[]){
		

		ModuleInstance instance = new ModuleInstance();
		instance.setModuleName("launcher");
		instance.setNamespace("java");
		try {
		    OcfaConfig config = new OcfaModuleConfig(instance);

			OcfaModuleLauncher launcher = new OcfaModuleLauncher();
			launcher.loadLibraries(config.getProperty("javalibraries").split(","));
			String javaModules[];
			if (argv.length > 0){
				
					javaModules = new String[1];
					javaModules[0] = argv[0];
					
			} else {
				
				logger.info("javasplit is " + config.getProperty("javamodules").split(","));
				logger.info("javamodules is " + config.getProperty("javamodules"));
				javaModules = config.getProperty("javamodules").split(",");
			}
			logger.info("javamodules is " + javaModules);
			launcher.startModules(javaModules);
		
		} catch(OcfaException e) {
			logger.error(e.getMessage());
			
		}
	}
}
