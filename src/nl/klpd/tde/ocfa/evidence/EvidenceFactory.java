package nl.klpd.tde.ocfa.evidence;

import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.store.DigestPair;
/**
 * Factory interace for creating evidences.
 * @author joep
 *
 */
public interface EvidenceFactory {

	/**
	 * Creates an evidence based upon a string. This is normally used to create interfaces from 
	 * metastoreentities.
	 * @param inEvidenceString String describing an interfaces
	 * @return evidence.
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inEvidenceString) throws OcfaException;
	
	/**
	 * Returns the evidences as a string. This is used to update metastoreentities.
	 * @param inEvidence
	 * @return
	 * @throws OcfaException
	 */
	public String evidenceAsString(Evidence inEvidence) throws OcfaException;
	
	
	/**
	 * Creates an evidence, using a parentevidence and minimal information for a new evidence.
	 * @param inDataHandle can be null, the handle to the storeentity containing the data about the
	 * @param inDigestPair can be null, the digests of the data.
	 * @param inEvidenceName the name of the file, or dir of the evidence. 
	 * @param inParentEvidence the evidence from which this evidence is derived. 
	 * @param inParendChildRelation the relatation between the parentevidende and this evidence.
	 * @return a new evidence. 
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inDataHandle, DigestPair inDigestPair,
									String inEvidenceName, Evidence inParentEvidence, 
									String inParendChildRelation) throws OcfaException;
	
	/**
	 * Creates an evidence but without a parentevidence. The evidence just start. 
	 * @param inDataHandle can be null, the handle to the storeentity containing the data about the
	 * @param inDigestPair can be null, the digests of the data.
	 * @param inEvidenceName the name of the file, or dir of the evidence. 
	 * @param inCaseName the name of the case.
	 * @param inSource the evidence source in which this evidence. 
	 * @param inItem the itemname of the item.
	 * @return the evidence. 
	 * @throws OcfaException
	 */
	public Evidence createEvidence(String inDataHandle, DigestPair inDigestPair,
								   String inEvidenceName, String inCaseName, String inSource,
								   String inItem) throws OcfaException;
}
