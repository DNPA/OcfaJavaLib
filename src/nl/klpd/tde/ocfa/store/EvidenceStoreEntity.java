package nl.klpd.tde.ocfa.store;

public class EvidenceStoreEntity {

	private DigestPair pair;
	private String handle;
	
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public DigestPair getPair() {
		return pair;
	}
	public void setPair(DigestPair pair) {
		this.pair = pair;
	}
}
