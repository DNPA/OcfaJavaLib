package nl.klpd.tde.ocfa.misc;
/**
 * Utility class to store information necessary to identify an evidence.
 * @author joep
 * @codereview jochen
 *
 */
public class EvidenceIdentifier {
    private String caseName;
    private String evidenceSource;
    private String itemName;
    private String evidenceID;
    
	public String getCaseName() {
		return caseName;
	}
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	public String getEvidenceSource() {
		return evidenceSource;
	}
	public void setEvidenceSourceID(String evidenceSource) {
		this.evidenceSource = evidenceSource;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getEvidenceID() {
		return evidenceID;
	}
	public void setEvidenceID(String evidenceID) {
		this.evidenceID = evidenceID;
	}

}
