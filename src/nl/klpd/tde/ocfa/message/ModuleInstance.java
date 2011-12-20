package nl.klpd.tde.ocfa.message;

/**
 * 
 * Module Instance.
 * @author joep
 * @codereview jochen
 */
public class ModuleInstance {

	private String host;
	private String instance;
	private String namespace;
	private String moduleName;
	private int port;
	
	
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ModuleInstance(){
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public boolean equals(Object inObject){
		
		if (inObject != null && inObject instanceof ModuleInstance){
			
			ModuleInstance other = (ModuleInstance) inObject;
			return ((other.getHost().equals(getHost()))
					&& (other.getInstance().equals(getInstance()))
					&& (other.getModuleName().equals(getModuleName()))
					&& (other.getNamespace().equals(getNamespace())));
		
		} else {
			return false;
		}
		
	}
	/**
	 * getModuleTypeOfInstance
	 * Get the global type name and Namespace of an Module instance
	 * to send a replay to the sender type of an instance
	 * @return ModuleInstance
	 */
	public ModuleInstance getModuleTypeOfInstance(){
	    
		ModuleInstance anyModuleInstance = new ModuleInstance();
		anyModuleInstance.setModuleName(getModuleName());
		anyModuleInstance.setNamespace(getNamespace());
		return anyModuleInstance;
	}
	
	public String toString(){
	
		return getHost() +":" + getModuleName() + ":" + getInstance();
	}
	
}
