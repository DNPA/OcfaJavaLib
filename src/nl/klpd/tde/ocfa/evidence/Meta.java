package nl.klpd.tde.ocfa.evidence;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;


import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * object representing a piece of metadata. For now, only string metadata are allowed.
 * @author joep
 *
 */
public class Meta {
	private String name;
	private String value;
	private ValueType type;
	
	private Logger log = Logger.getLogger(this.getClass());
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	
	public Meta(ValueType inType){
		
		type = inType;
	}
	
	public Meta(String inName, String inValue) throws OcfaException{
		
		name = inName;
		value = checkStringValue(inValue);
		type = ValueType.STRING;
	}

	/**
	 * Checks whether the value is appropriate to be included in a metavalue. It will change the
	 * value if necessary. The value is changed if a zero is encountered in the string.
	 * @param inValue the 
	 * @return the value changed if necessary
	 * @throws OcfaException
	 */
    public static String checkStringValue(String inValue) throws OcfaException {
		
    	String value = inValue;
    	if (inValue.indexOf(0) != -1){
    		
    		value = inValue.substring(0, inValue.indexOf(0));
    	}
    	for (int i = 0; i < value.length(); i++){
    		
    		if (Character.isISOControl(inValue.charAt(i))){
    			
    			throw new OcfaException("Illegal character at position" + inValue);
    		}
    	}
		return value;
	}

	public Meta(String inName, String inValue, ValueType inType) throws OcfaException {
		name = inName;
		value = checkStringValue(inValue);
		type = inType;		
    }


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) throws OcfaException {
		this.value =checkStringValue(value);
	}
	public Date getDate() throws OcfaException{
		
		assertType(ValueType.DATETIME);
		if (value == null){
			
			return null;
		} else {
			
			try {
				return dateFormat.parse(value.substring(0, value.lastIndexOf(':')));
			} catch (ParseException e) {
				
				log.error(e.getMessage());
				throw new OcfaException(e.getMessage());
			}
		}
	}

	
	
	
	private void assertType(ValueType valueType) throws OcfaException{
		
		if (type != valueType){
			
			throw new OcfaException("Tried to retrieve " + valueType + " from " + value);
		}
		
	}

	public long getInt() throws OcfaException {
		
		assertType(ValueType.INT);
		return Long.parseLong(value);
	}
	
	
	protected String getDateString(){
		
		return value.substring(0, value.lastIndexOf(':'));
	}
	
	public void setDate(Date inDate) throws OcfaException {
		
		String source = getSource();
		value = dateFormat.format(inDate) + ":" + source;
        type = ValueType.DATETIME;
	}
	
	public ValueType getType() {
		return type;
	}
	public void setType(ValueType type) {
		this.type = type;
	}
	
	public String getSource() throws OcfaException {
	  if (type == ValueType.DATETIME) {
		if (value != null && value.lastIndexOf(':')  > 0){
			
			return value.substring(value.lastIndexOf(':') + 1);
		} else {
			
			return "INVALID";
		}
      } else {
            log.error("Trying to retieve a DateTime source from a non DATETIME value");
			throw new OcfaException("Retrieving DataTime source from a non DATETIME value");
      }
	}
	
	public void setSource(String inSource) throws OcfaException {

        //JCW: Check for ":" in inSource, otherwise possible parsing problems for lastIndexOf(':') searches
        if(inSource.contains(":")) {
            log.error("Source string contains invalid character \":\"");
            throw new OcfaException("Source string contains invalid character \":\"");
        }
		value = getDateString() + ":" + inSource;
		
	}
	
}
