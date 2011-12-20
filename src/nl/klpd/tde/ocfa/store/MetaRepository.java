package nl.klpd.tde.ocfa.store;

import java.io.InputStream;

import nl.klpd.tde.ocfa.misc.OcfaException;
/**
 * interface to the metarepository. Previously used to distinguish between 20 and 21 repositories. 
 * 
 * @author joep
 * @TODO either remove this distinction or add a similar distinction for the normal repository.
 */
public interface MetaRepository {
	
	public abstract InputStream getMetaEntityAsStream(String inReference)
		throws OcfaException;

	public abstract String getMetaAsString(String inReference)
			throws OcfaException;

	public abstract void updateMetaStoreEntity(String inReference,
			String inNewContent) throws OcfaException;


	public String insertMetaStoreEntity(String inMeta) throws OcfaException;
}
