package nl.klpd.tde.ocfa.evidence;

import java.util.Iterator;

import nl.klpd.tde.ocfa.misc.OcfaException;
/**
 * Standard interface for an evidence. An evidence normally describes a file or a directory 
 * found on a disk or derived by a module.
 * @author joep
 *
 */
public interface Evidence extends EvidenceInfo{

	
	public String getEvidenceId();
	
	public String getEvidenceName() throws OcfaException;
	
	public String getEvidencePath() throws OcfaException;
	
	public int getJobCount();
	/**
	 * Returns an iterator of the different jobs. An evidence consists of some general metadata 
	 * and Jobs, each job described the passing of an evidence through a module.
	 * 
	 */	
	public  Iterator<Job> getJobIterator();
	
	public Job getActiveJob();
	
	public String getDataHandle();
	/**
	 * Allows new metadata to be added to the evidence. Initiates the last job. It 
	 * assumes that the last job was made by the router for this module.
	 */
	
	public void setMutable();
	
	public String getCase();
	
	public String getSource();
	
	public String getItem();
	/**
	 * Goes through all metavalues and tries to find the first one with a certain name.
	 */
	public String getMetaValue(String inMeta);

	public String getMd5();
	
	public String getSha1();
	
	
	public Iterator<ChildEvidence> getChildIterator();
}
