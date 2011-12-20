package nl.klpd.tde.ocfa.evidence.dom;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import nl.klpd.tde.ocfa.evidence.ValueType;
import nl.klpd.tde.ocfa.evidence.Job;
import nl.klpd.tde.ocfa.evidence.Meta;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Implements a implementation of a Job for use with dom evidencses. 
 * @author joep
 *
 */

public class DomJob implements Job {

	private static Log logger = LogFactory.getLog(DomJob.class);
	/**
	 * The specific element in the evidence domtree, that represents this Job
	 */
	private Element jobElement;
	static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	public DomJob(Element inJobElement) {

		jobElement = inJobElement;	
	}


	public void close() {

		jobElement.setAttribute("status", "PROCESSED");
		if (jobElement.getAttribute("stime") != null){

			setStopTime();
		}

	}

	public Iterator<Meta> getMetaIterator() {
		// TODO Auto-generated method stub
		return new MetaIterator();
	}

	public void setStartTime(){

		String currentTime = dateFormat.format(System.currentTimeMillis());
		jobElement.setAttribute("stime", currentTime);
	}


	public Date getStartTime() throws OcfaException{

		return getTime("stime");

	}


	protected Date getTime(String inTimeAttribute) throws OcfaException{

		String timeString = jobElement.getAttribute(inTimeAttribute);
		if (timeString == null || timeString.length() == 0){

			return null;
		} else {

			try {
				return dateFormat.parse(timeString);
			} catch (ParseException e) {

				throw new OcfaException(e.getMessage());
			}
		}

	}

	protected Integer getTimeLength(String inAttribute) {

		String timeLengthString = jobElement.getAttribute(inAttribute);
		Integer timeLength;
		if (timeLengthString == null|| timeLengthString.length() == 0){

			return null;
		} else {

			try {
				timeLength = new Integer(timeLengthString);
			} catch(NumberFormatException e){

				logger.error("error converting " + timeLengthString);
				return null;
			}
			return timeLength;

		}
	}

	public Integer getProfInJobTime(){

		return getTimeLength("profinjobtime");
	}

	public Integer getRealInJobTime(){

		return getTimeLength("realinjobtime");
	}

	public void setStopTime(){

		String currentTime = dateFormat.format(System.currentTimeMillis());
		jobElement.setAttribute("etime", currentTime);
	}


	public Date getStopTime() throws OcfaException{

		return getTime("etime");
	}
	/**
	 * Sets the moduleinstance information in the job. used in evidence.setMutable.
	 * @param inInstance
	 */
	public void setModuleInstance(ModuleInstance inInstance){

		Element moduleInstanceElement  = jobElement.getOwnerDocument().createElement("moduleinstance");
		moduleInstanceElement.setAttribute("host", inInstance.getHost());
		moduleInstanceElement.setAttribute("namespace", inInstance.getNamespace());
		moduleInstanceElement.setAttribute("module", inInstance.getModuleName());
		moduleInstanceElement.setAttribute("instance", inInstance.getInstance());
		jobElement.appendChild(moduleInstanceElement);		
	}

	public ModuleInstance getModuleInstance(){

		NodeList moduleElements = jobElement.getElementsByTagName("moduleinstance");
		if (moduleElements.getLength() > 0){

			if (moduleElements.getLength() > 1){

				logger.warn("more than one moduleinstance found taking first ");
			}
			Element moduleElement = (Element)moduleElements.item(0);
			ModuleInstance instance = new ModuleInstance();
			instance.setHost(moduleElement.getAttribute("host"));
			instance.setNamespace(moduleElement.getAttribute("namespace"));
			instance.setModuleName(moduleElement.getAttribute("module"));
			instance.setInstance(moduleElement.getAttribute("instance"));
			return instance;
		}
		else {

			return null;
		}

	}
	public void setMeta(String inName, String inValue) throws OcfaException{

		setMeta(inName, inValue, ValueType.STRING);
	}

	public void setMeta(String inName, String inValue, ValueType inType) throws OcfaException {

		String value = Meta.checkStringValue(inValue);
		Element metaElement = jobElement.getOwnerDocument().createElement("meta");
		metaElement.setAttribute("name", inName);
		metaElement.setAttribute("type", "scalar");
		Element scalarElement = jobElement.getOwnerDocument().createElement("scalar");
		scalarElement.setAttribute("type", inType.getDescription());
		scalarElement.setTextContent(value);
		metaElement.appendChild(scalarElement);
		jobElement.appendChild(metaElement);	
	}

	public int getChildCount(){

		return jobElement.getElementsByTagName("childevidence").getLength();

	}

	/**
	 * iteratror implementation for the metaElmeents, which is a nodelist.
	 * @author joep
	 *
	 */
	protected class MetaIterator implements Iterator<Meta>{

		private NodeList metaElements;
		private int nextIndex = 0;
		public MetaIterator(){

			metaElements = jobElement.getElementsByTagName("meta");			
		}

		public boolean hasNext() {
			// TODO Auto-generated method stub
			return nextIndex < metaElements.getLength();
		}

		public Meta next() {

			try {
				Element metaElement = (Element)metaElements.item(nextIndex);
				nextIndex++;
				String name = metaElement.getAttribute("name");
				String type = metaElement.getAttribute("type");
				if (type.equalsIgnoreCase("scalar")){

					return createScalarMeta(metaElement, name);
				} else {
					
					return createComplexMeta(metaElement, name);
				} 
			} catch(OcfaException e){

			throw new RuntimeException("Error while processing faulty xml ??? " + e.getMessage());
		}
	}

	private Meta createComplexMeta(Element metaElement, String name) throws OcfaException {

		NodeList list = metaElement.getElementsByTagName("scalar");
		StringBuffer metaText = new StringBuffer("");
		for (int x =0; x < list.getLength(); x++){
		
			Element scalar = (Element)list.item(x);
			metaText.append(scalar.getTextContent());
			if (x < list.getLength() -1){
				
				metaText.append(", ");
			}

		}
		return new Meta(name, metaText.toString());
	}

	private Meta createScalarMeta(Element metaElement, String name)
	throws OcfaException {
		Element scalarElement = (Element) metaElement.getElementsByTagName("scalar").item(0);
		if (scalarElement == null){

			return new Meta(name, null);
		}
		String value = scalarElement.getTextContent();
		String type = scalarElement.getAttribute("type");

		if(type.equals("datetime")) {
			return new Meta(name,value,ValueType.DATETIME);
		} else if(type.equals("string")) {
			return new Meta(name, value);
		} else if (type.equals("int")){
			
			return new Meta(name, value, ValueType.INT);
			
		}else {
			logger.error("Scalartype " + type + " not handled");
			return new Meta(name,value);
		}
	}

	public void remove() {

	}

}

//@Override
public void addChild(String inIdentifier, String inChildName, String inParentChildRelation) {

	Element newElement = this.jobElement.getOwnerDocument().createElement("childevidence");
	newElement.setAttribute("evidenceid", inIdentifier);
	newElement.setAttribute("name", inChildName);
	newElement.setAttribute("relname", inParentChildRelation);
	this.jobElement.appendChild(newElement);		
}

}
