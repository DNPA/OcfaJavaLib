package nl.klpd.tde.ocfa.evidence.dom;

import java.util.Iterator;

import nl.klpd.tde.ocfa.evidence.ChildEvidence;
import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.evidence.Job;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;

import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Evidence that uses a domtree to represent a description of a piece of metadata.. 
 * @author joep
 *
 */
public class DomEvidence implements Evidence {


	private Document domDocument;
	
	// cached internal list of job elements. Used to speed up things.
	private NodeList jobElementList;
	// the ownaddress. This is used when the evidence is made mutable. 
	private ModuleInstance ownAddress;
	private Logger log = Logger.getLogger(this.getClass());
	public DomEvidence(Document document, ModuleInstance inAddress) {
		
		domDocument = document;
		jobElementList = document.getElementsByTagName("job");
		ownAddress = inAddress;
	}

	/**
	 * Gets the last job. This one is supposed to be active in a module. 
	 */
	public Job getActiveJob() {
		// TODO Auto-generated method stub
		Element lastJob = (Element)jobElementList.item(jobElementList.getLength() - 1);
		return new DomJob(lastJob);
	}

	
	public Document getDomDocument() {
		return domDocument;
	}


	public void setDomDocument(Document domDocument) {
		this.domDocument = domDocument;
	}


	public String getDataHandle() {
		// TODO Auto-generated method stub
		String dataref = domDocument.getDocumentElement().getAttribute("storeref");
		return (dataref != null && dataref.length() > 0) ? dataref : null;
	}


	public String getEvidenceId() {
		// TODO Auto-generated method stub
		return domDocument.getDocumentElement().getAttribute("id");
	}

	protected Element getLocationElement() throws OcfaException{
		
		Element element = (Element)(domDocument.getElementsByTagName("location").item(0));
		if (element == null){
			
			throw new OcfaException("no element tag in this evidence");
		}
		return element;
	}
	
	public String getEvidenceName() throws OcfaException {
		// TODO Auto-generated method stub
	
		Element element = getLocationElement();
		return element.getAttribute("name");
		
	}

	public String getEvidencePath() throws OcfaException {
		// TODO Auto-generated method stub
		Element element = getLocationElement();
		return element.getTextContent();
		
		
	}

	
	public int getJobCount() {
		// TODO Auto-generated method stub
		return jobElementList.getLength();
	}

	/**
	 * Returns an iterator of the different jobs. 
	 * @see nl.klpd.tde.ocfa.evidence.Evidence#getJobIterator()
	 */
	public Iterator<Job> getJobIterator() {
		// TODO Auto-generated method stub
		return new JobIterator();
	}

	/**
	 * Allows new metadata to be added to the evidence. Initiates the last job. It 
	 * assumes that the last job was made by the router for this module.
	 */
	public void setMutable() {
		
		DomJob lastJob = (DomJob)getActiveJob();
		lastJob.setModuleInstance(ownAddress);
		lastJob.setStartTime();
	}
	
	/**
	 * Adapter class for having an iterator over the jobelementlist.
	 * @author joep
	 *
	 */
	protected class JobIterator implements Iterator<Job>{

		int nextIndex = 0;
		
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return nextIndex < jobElementList.getLength();
		}

		
		public Job next() {
			// TODO Auto-generated method stub
			Job job = new DomJob((Element)jobElementList.item(nextIndex));
			nextIndex++;
			return job;
			
		}

		
		public void remove() {
			
			throw new UnsupportedOperationException("removal of job elements not implemented");
			
		}
		
		
		
	}


	//@Override
	public String getCase() {

		return domDocument.getDocumentElement().getAttribute("case");
	}


	//@Override
	public String getItem() {

		return domDocument.getDocumentElement().getAttribute("item");
	}


	//@Override
	public String getSource() {

		return domDocument.getDocumentElement().getAttribute("src");
	}

	/**
	 * Goes through all metavalues and tries to find the first one with a certain name.
	 * 
	 * This is a pretty expensive operation. It might be useful to create a caching mechanism for this.
	 */
	public String getMetaValue(String inMetaName) {
		// TODO Auto-generated method stub
		String metaValue = null;
		NodeList metaElements = domDocument.getElementsByTagName("meta");
		for (int x = 0; x < (metaElements.getLength()) && (metaValue == null); x++){
			
			Element metaElement = (Element) metaElements.item(x);
			if (metaElement.getAttribute("name").equalsIgnoreCase(inMetaName)){
				
				NodeList metaList = metaElement.getElementsByTagName("scalar");
				if (metaList != null && metaList.getLength() > 0){
				
					metaValue = metaElement.getElementsByTagName("scalar").item(0).getTextContent();
				} else {
					
					log.error("found metaname with no scalar for " + inMetaName);
				}
			}
		}
		return metaValue;
		
		
	}

	public String getMd5() {
		
		return domDocument.getDocumentElement().getAttribute("md5");

	}

	public String getSha1() {
		
		return domDocument.getDocumentElement().getAttribute("sha");

	}

	
	public Iterator<ChildEvidence> getChildIterator(){
		
		return new DomChildIterator();
	}

	protected class DomChildEvidence implements ChildEvidence {

		private Element childElement;
		
		public DomChildEvidence(Element inChildElement){
			
			childElement =  inChildElement;
			
		}
		
		public String getCase() {
			// TODO Auto-generated method stub
			return DomEvidence.this.getCase();
		}

		public String getEvidenceId() {
			// TODO Auto-generated method stub
			return childElement.getAttribute("evidenceid");
		}

		public String getEvidenceName() throws OcfaException {
			// TODO Auto-generated method stub
			return childElement.getAttribute("name");
		}

		public String getItem() {
			// TODO Auto-generated method stub
			return DomEvidence.this.getItem();
		}

		public String getSource() {
			// TODO Auto-generated method stub
			return DomEvidence.this.getSource();
		}

		public String getRelation() {
			// TODO Auto-generated method stub
			return childElement.getAttribute("relname");
		}

	}
	
	protected class DomChildIterator implements Iterator<ChildEvidence>{

		NodeList childElements;
		int current = -1;
		public DomChildIterator(){
			
			 childElements = domDocument.getElementsByTagName("childevidence");
		}
		
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return childElements.getLength() > (current + 1);		}

		public ChildEvidence next() {
			// TODO Auto-generated method stub
			current++;
			return new DomChildEvidence((Element)childElements.item(current));
		}

		public void remove() {
			throw new RuntimeException("remove not supported");
			
		}
		
		
		
	}


}
