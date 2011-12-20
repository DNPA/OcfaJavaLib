package nl.klpd.tde.ocfa.evidence;

import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * interface to store some but not all information about an evidence.
 * This can be used to refer to an evidence.
 * @author DIGIRECH\kl009834
 *
 */
public interface EvidenceInfo {

	public String getCase();
	
	public String getSource();
	
	public String getItem();

	public String getEvidenceId();
	
	public String getEvidenceName() throws OcfaException;
}
