package nl.klpd.tde.ocfa.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


import nl.klpd.tde.ocfa.message.ModuleInstance;
/**
 * Wrapper around properties for retrieving ocfa configuration stuff.
 * @author joep
 * @codereview jochen
 * @todo Implement module specific stuff.
 */
public class OcfaModuleConfig implements OcfaConfig {
	
	private Properties ocfaProps;
	
	private ModuleInstance mCurrentInstance;

        private String mOcfaRoot; 	
	
        private String mOcfaEtc;

	public OcfaModuleConfig(ModuleInstance inCurrentInstance) 
	throws OcfaException {
		
		String ocfaCase = System.getenv("OCFACASE");
                mOcfaRoot = System.getenv("OCFAROOT");
		if (mOcfaRoot == null){
			mOcfaRoot = "/usr/local/digiwash";
		}
		if(ocfaCase == null) {
			throw new OcfaException("No valid case retrieved from ENVIRONMENT");
		}
                mOcfaEtc = "/var/ocfa/cases/" + ocfaCase + "/etc";
		File  configFile = new File(mOcfaEtc + "/ocfa.conf");
		ocfaProps = new Properties();
		try {
			ocfaProps.load(new FileInputStream(configFile));
		} catch(FileNotFoundException e) {
			throw new OcfaException(e.getMessage());
		} catch(IOException e) {
			throw new OcfaException(e.getMessage());
		}
	
		mCurrentInstance = inCurrentInstance;
	}
	
	/* (non-Javadoc)
	 * @see nl.klpd.tde.ocfa.misc.OcfaConfig#getProperty(java.lang.String)
	 */
	public String getProperty(String key){
                if (key == "ocfaroot") {
                    return mOcfaRoot;
                }
                if (key == "ocfaetc") {
                    return mOcfaEtc;
                }
                String value = ocfaProps.getProperty(key);
                if ((value != null)&&(value != "")) {
                    StringBuilder builder=new StringBuilder(value);
                    int vindex=builder.indexOf("$OCFAROOT");
                    if (vindex > -1) {
                       builder.replace(vindex,vindex+"$OCFAROOT".length(),mOcfaRoot);
                    }
                    vindex=builder.indexOf("$OCFAETC");
                    if (vindex > -1) {
                       builder.replace(vindex,vindex+"$OCFAETC".length(),mOcfaEtc);
                    }
                    value=builder.toString();
                }
		return value;
	}

	/* (non-Javadoc)
	 * @see nl.klpd.tde.ocfa.misc.OcfaConfig#getProperty(java.lang.String, java.lang.String)
	 */
	public String getProperty(String key, String inDefault) {
	
		String value = ocfaProps.getProperty(key);
		if ((value == null)|| (value == "")){
			
			return inDefault;
		} else {
                        StringBuilder builder=new StringBuilder(value);
                        int vindex=builder.indexOf("$OCFAROOT");
                        if (vindex > -1) {
                           builder.replace(vindex,vindex+"$OCFAROOT".length(),mOcfaRoot);
                        }
                        vindex=builder.indexOf("$OCFAETC");
                        if (vindex > -1) {
                           builder.replace(vindex,vindex+"$OCFAETC".length(),mOcfaEtc);
                        }
                        value=builder.toString();
			return  value;
		}
	}

	protected Properties getOcfaProps() {
		return ocfaProps;
	}

	protected void setOcfaProps(Properties ocfaProps) {
		
		this.ocfaProps = ocfaProps;
	}

	public  ModuleInstance getMCurrentInstance() {
		return mCurrentInstance;
	}

	public  void setMCurrentInstance(ModuleInstance currentInstance) {
		mCurrentInstance = currentInstance;
	}

	public Boolean getBooleanProperty(String key) {
		// TODO Auto-generated method stub
		String value = getProperty(key);
		if (value == null){
			
			return null;
		} else if (value.equals("true") || value.equals("yes")){
			
			return Boolean.TRUE;
		} else if (value.equals("false") || value.equals("no")){
			
			return Boolean.FALSE;
		} else {
			
			return null;
		}
	}
}
