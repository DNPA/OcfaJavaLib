package nl.klpd.tde.ocfa.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import nl.klpd.tde.ocfa.misc.OcfaException;
/**
 * Not fully implementaed repository. Used for 2.0 stuff 
 * @author joep
 *
 */
class OcfaPgFileMetaRepository implements MetaRepository {

	public String repositoryRoot;
	public OcfaPgFileMetaRepository(String inRepositoryRoot){
		
		repositoryRoot = inRepositoryRoot;
	}
	
	public InputStream getMetaEntityAsStream(String inReference)
			throws OcfaException {
	
		try {
			return new FileInputStream(getFile(inReference));
		} catch (FileNotFoundException e){
		
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		}
	}

	protected File getFile(String inReference) {
		// TODO Auto-generated method stub
		return new File(repositoryRoot + inReference);
	}

	
	protected long getSize(String inReference){
		
		return getFile(inReference).length();
	}
	
	public String getMetaAsString(String inReference) throws OcfaException {
		// TODO Auto-generated method stub
		char buffer[] = new char[1000];
		if (getSize(inReference) > 1000000){
			
			throw new OcfaException("resource " + inReference + " too large");
		}
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(getMetaEntityAsStream(inReference));
		} catch (OcfaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException("cannot find " + inReference);
		}
		StringBuffer metaString = new StringBuffer();
		try {
			while (reader.ready()){		
			
				int length = reader.read(buffer, 0, 1000);
				metaString.append(buffer, 0, length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metaString.toString();
	}

	
	public void updateMetaStoreEntity(String inReference, String inNewContent)
			throws OcfaException {
		// TODO Auto-generated method stub
		
	}

	//Override
	
	public String insertMetaStoreEntity(String inMeta) throws OcfaException {
		// TODO Auto-generated method stub
		return null;
	}
}
