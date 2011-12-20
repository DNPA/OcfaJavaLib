package nl.klpd.tde.ocfa.store;

import java.io.File;
import java.io.InputStream;

import nl.klpd.tde.ocfa.misc.EvidenceIdentifier;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.store.OcfaRepository.RepositoryMode;

public interface Repository {

	public abstract String getMetaAsString(String inReference)
			throws OcfaException;

	public abstract InputStream getDataEntityAsStream(String inReference)
			throws  OcfaException;
	public String getDataEntityAsFile(String inDataId) 
		throws OcfaException;
	public abstract InputStream getMetaEntityAsStream(String inReference)
			throws OcfaException;

	public abstract void updateMetaStoreEntity(String inReference, String inNewContent) throws OcfaException;

	public String insertMeta(String inMeta, String inDataId, EvidenceIdentifier inIdentifier) 
		throws OcfaException;
	
	public EvidenceStoreEntity createEvidenceStoreEntity(File inFile) throws OcfaException;
	
	public EvidenceStoreEntity createEvidenceStoreEntity(InputStream inStream) throws OcfaException;
	
	public void createItem(String inCaseName, String inSource, String inItem) throws OcfaException;
	
}