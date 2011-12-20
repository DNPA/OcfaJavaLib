package nl.klpd.tde.ocfa.evidence.dom;

import org.w3c.dom.Element;

import nl.klpd.tde.ocfa.evidence.ChildEvidence;
import nl.klpd.tde.ocfa.evidence.EvidenceInfo;
import nl.klpd.tde.ocfa.misc.OcfaException;

public class DomChildEvidence implements ChildEvidence {

	private Element childElement;
	private DomEvidence parent;
	public DomChildEvidence(Element inChildElement, DomEvidence inParent){
		
		childElement =  inChildElement;
		
	}
	
	public String getCase() {
		// TODO Auto-generated method stub
		return null;
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
		return null;
	}

	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRelation() {
		// TODO Auto-generated method stub
		return null;
	}

}
