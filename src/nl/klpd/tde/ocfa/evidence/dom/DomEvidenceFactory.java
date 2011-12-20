package nl.klpd.tde.ocfa.evidence.dom;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.evidence.EvidenceFactory;
import nl.klpd.tde.ocfa.message.DefaultMessageFactory;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.misc.XmlUtil;
import nl.klpd.tde.ocfa.store.DigestPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * Implementation of an evidencefactory that uses DOM to create evidences.
 * @author joep
 *
 */
public class DomEvidenceFactory implements EvidenceFactory {

	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private ModuleInstance ownAddress;
	private static Log logger = LogFactory.getLog(DomEvidenceFactory.class);

	/**
	 * Creates DocumentBuilderFactory and DocumentBuilder
	 * @param ownAddress the moduleinstance of the current module. Eventually used to make
	 * evidence mutable.
	 * @throws OcfaException
	 */
	public DomEvidenceFactory(ModuleInstance ownAddress) throws OcfaException{
		
		this.ownAddress = ownAddress;
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("cannot create document builder " + e.getMessage() );
			throw new OcfaException(e.getMessage());
		} 
		
	}
	
	/**
	 * Creates an evidence based upon a string. This is normally used to create interfaces from 
	 * metastoreentities. 
	 * 
	 * It makes a domdocument from the string and initializes a DomeEvidence with it.
	 * @param inEvidenceString String describing an interfaces
	 * @return evidence.
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inEvidenceString) throws OcfaException {
		// TODO Auto-generated method stub
		Document document;
		try {
			document = builder.parse(new InputSource(new StringReader(inEvidenceString)));
			return new DomEvidence(document, ownAddress);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());

		} catch (IOException e) {
			e.printStackTrace();
			logger.error("error parsing " + inEvidenceString);
			throw new OcfaException(e.getMessage());
		}
		
	}

	/**
	 * Transforms the evidence back to a string. 
	 */
	public String evidenceAsString(Evidence inEvidence) throws OcfaException {
		
		if (!(inEvidence instanceof DomEvidence)){
			
			throw new OcfaException("expected Domevidence, found: " +  inEvidence.getClass());
		}
		try {
			return XmlUtil.domToText(((DomEvidence)inEvidence).getDomDocument());
		} catch (TransformerException e) {
			
			e.printStackTrace();
			throw new OcfaException("cannot convert " + inEvidence.getEvidenceId() + " to text");
		}
	}

	/**
	 * Creates an evidence, using a parentevidence and minimal information for a new evidence.
	 * @param inDataHandle can be null, the handle to the storeentity containing the data about the
	 * @param inDigestPair can be null, the digests of the data.
	 * @param inEvidenceName the name of the file, or dir of the evidence. 
	 * @param inParentEvidence the evidence from which this evidence is derived. 
	 * @param inParendChildRelation the relation between the parentevidende and this evidence.
	 * @return a new evidence. 
	 * 
	 * Creates a empty domdocuments and fills the given information into the domtree.
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inDataHandle,
			DigestPair inDigestPair, String inEvidenceName,
			Evidence ioParentEvidence, String inParendChildRelation) throws OcfaException {
		// TODO Auto-generated method stub
		
		Document document = builder.newDocument();
		Element element = initEvidenceElement(document.createElement("evidence"), inDataHandle,
												inDigestPair, ioParentEvidence);
		document.appendChild(element);
		element.appendChild(initLocationNode(document.createElement("location"), ioParentEvidence, inEvidenceName));
		
		element.appendChild(document.createElement("job"));
		
		Evidence evidence = new DomEvidence(document, ownAddress);
		ioParentEvidence.getActiveJob().addChild(evidence.getEvidenceId(), evidence.getEvidenceName(), inParendChildRelation);
		evidence.setMutable();		
		return evidence;
	}

	/**
	 * Helper method that initializes a location node. The location nodes is something like:
	 * <location name="bar">/home/foo</location>
	 * @param ioLocationElement
	 * @param inParentEvidence
	 * @param inEvidenceName
	 * @return
	 * @throws OcfaException
	 */
	protected Node initLocationNode(Element ioLocationElement,
			Evidence inParentEvidence, String inEvidenceName) throws OcfaException {
		
		ioLocationElement.setAttribute("name", inEvidenceName);
		if (inParentEvidence != null){
			String location = inParentEvidence.getEvidencePath() + "/" + inParentEvidence.getEvidenceName(); 
			ioLocationElement.appendChild(ioLocationElement.getOwnerDocument().createTextNode(location));
		}
		return ioLocationElement;
	}

	/**
	 * Helper element that initializes the main evidence element. Something like
	 * <evidence md5="blash" sha1="bsad" storeref="123" case="foo" src="bar" item="baz"></evidence>
	 * @param ioEvElement
	 * @param inDataHandle
	 * @param inDigestPair
	 * @param inParentEvidence
	 * @return
	 */
	protected Element initEvidenceElement(Element ioEvElement,
			String inDataHandle, DigestPair inDigestPair,
			Evidence inParentEvidence) {
		// TODO Auto-generated method stub
		ioEvElement.setAttribute("id", generateId(inParentEvidence));
		if (inDigestPair != null){
			
			ioEvElement.setAttribute("md5", inDigestPair.getMd5());
			ioEvElement.setAttribute("sha", inDigestPair.getSha1());
			ioEvElement.setAttribute("storeref", inDataHandle);
		}
		ioEvElement.setAttribute("case", inParentEvidence.getCase());
		ioEvElement.setAttribute("src", inParentEvidence.getSource());
		ioEvElement.setAttribute("item", inParentEvidence.getItem());
		ioEvElement.setAttribute("status", "NEW");
		
		return ioEvElement;
	}
	/**
	 * Helper element that initializes the main evidence element. Something like
	 * <evidence md5="blash" sha1="bsad" storeref="123" case="foo" src="bar" item="baz"></evidence>
	 * 
	 * @param ioEvElement
	 * @param inDataHandle
	 * @param inDigestPair
	 * @param inCaseName
	 * @param inSourceName
	 * @param inItemName
	 * @return
	 */
	protected Element initEvidenceElement(Element ioEvElement,
			String inDataHandle, DigestPair inDigestPair,
			String inCaseName, String inSourceName, String inItemName) {
		// TODO Auto-generated method stub
		ioEvElement.setAttribute("id", "0");
		if (inDigestPair != null){
			
			ioEvElement.setAttribute("md5", inDigestPair.getMd5());
			ioEvElement.setAttribute("sha", inDigestPair.getSha1());
			ioEvElement.setAttribute("storeref", inDataHandle);
		}
		ioEvElement.setAttribute("case", inCaseName);
		ioEvElement.setAttribute("src",inSourceName);
		ioEvElement.setAttribute("item", inItemName);
		ioEvElement.setAttribute("status", "NEW");
		
		return ioEvElement;
	}

	
	/**
	 * Helper method that generates a id using the parentevidence.
	 * @param inParentEvidence
	 * @return
	 */
	private String generateId(Evidence inParentEvidence) {
		// TODO Auto-generated method stub
		
		return inParentEvidence.getEvidenceId() + ".j" 
			+ (inParentEvidence.getJobCount() -1)+ "e" 
			+ inParentEvidence.getActiveJob().getChildCount();
	}
	/**
	 * 
	
	 * Creates an evidence but without a parentevidence. The evidence just start. 
	 * @param inDataHandle can be null, the handle to the storeentity containing the data about the
	 * @param inDigestPair can be null, the digests of the data.
	 * @param inEvidenceName the name of the file, or dir of the evidence. 
	 * @param inCaseName the name of the case.
	 * @param inSource the evidence source in which this evidence. 
	 * @param inItem the itemname of the item.
	 * @return the evidence. 
	 * @throws OcfaException
	 * 
	 * Creates a empty domdocuments and fills the given information into the domtree.
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inDataHandle,
			DigestPair inDigestPair, String inEvidenceName, String inCaseName,
			String inSource, String inItem) throws OcfaException {
		// TODO Auto-generated method stub
		
		Document document = builder.newDocument();
		Element element = initEvidenceElement(document.createElement("evidence"), inDataHandle,
												inDigestPair, inCaseName, inSource, inItem);
		document.appendChild(element);
		element.appendChild(initLocationNode(document.createElement("location"), null, inEvidenceName));
		
		element.appendChild(document.createElement("job"));
		
		Evidence evidence = new DomEvidence(document, ownAddress);
		evidence.setMutable();		
		return evidence;
	}
}


