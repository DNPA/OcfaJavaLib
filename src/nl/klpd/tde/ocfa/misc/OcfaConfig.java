package nl.klpd.tde.ocfa.misc;

public interface OcfaConfig {

	public abstract String getProperty(String key);

	public abstract String getProperty(String key, String inDefault);

	public abstract Boolean getBooleanProperty(String key);
	
}