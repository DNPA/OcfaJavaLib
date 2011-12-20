package nl.klpd.tde.ocfa.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import nl.klpd.tde.ocfa.misc.EvidenceIdentifier;
import nl.klpd.tde.ocfa.misc.OcfaConfig;
import nl.klpd.tde.ocfa.misc.OcfaException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * implementation of a repository for use within ocfa. It takes the path in the repsotiroy as 
 * reference and returns the content as a stream or as a string.
 * @author joep
 *
 */
public class OcfaRepository implements Repository {

	private static String INSERT_INTO_EVIDENCESTOREENTITY = "insert into evidencestoreentity (repname) Values (?)";
	private static String CREATE_ITEM = "insert into item (casename, evidencesource, item) values(?, ?, ?)";
	
	private static String GET_CURR_VALL = "select currval('evidencestoreentity_id_seq') as curval";
	private static String INSERT_INTO_METADATAINFO = "insert into metadatainfo(metadataid, dataid, evidence, itemid) VALUES ( ?, ?, ?, ?)";
	private static String GET_ITEM_ID = "select itemid from item where casename = ? and evidencesource = ? and item = ?";
	private File repositoryRoot;
	private MetaRepository metaRepos;
	private static Log logger = LogFactory.getLog(OcfaRepository.class);
	private DatabasePool pool;
	private RepositoryMode mode = RepositoryMode.MOVE;
	public enum RepositoryMode{
		
			COPY,
			MOVE
	};

	
	public OcfaRepository(OcfaConfig inConfig, DatabasePool inPool) throws OcfaException {

		repositoryRoot = new File((String)inConfig.getProperty("repository"));
		String version = inConfig.getProperty("version");
        String storeType = inConfig.getProperty("storeimpl");
        if(storeType.equals("pgblob")) {
			logger.info("starting ocfa PgBlob metarepository");
			metaRepos = new OcfaPgBlobMetaRepository(inPool);
			pool = inPool;

        } else if(storeType.equals("pgfile")) {
			logger.info("starting ocfa PgFile metarepository");
			metaRepos = new OcfaPgFileMetaRepository(repositoryRoot.getAbsolutePath());
			pool = inPool;

        } else {
            logger.error("Store implementation type" + storeType + " not supported");
            throw new OcfaException("Store implementation type" + storeType + " not supported");
        }


	
	}
	
	/**
	 * returns the content of a reference (a path in the repository) as a string.
	 * @see nl.klpd.tde.duif.model.Repository#getMetaAsString(java.lang.String)
	 */
	public String getMetaAsString(String inReference) throws OcfaException {
		
		return metaRepos.getMetaAsString(inReference);
		
	}

	
	/**
	 * returns the size of the resource.
	 * @param inReference
	 * @return
	 */
	protected  long getSize(String inReference) {
		// TODO Auto-generated method stub
		return getFile(inReference).length();
		
	}
	/**
	 * Returns the evidencestore resource as  a file. 
	 * @param inReference
	 * @return
	 */
	private File getFile(String inReference) {
		// TODO Auto-generated method stub
		return new File(repositoryRoot + inReference);
	}

	/** Returns the metaentity as a steram
	 * 
	 */
	public InputStream getMetaEntityAsStream(String inReference)throws OcfaException {
		
		return metaRepos.getMetaEntityAsStream(inReference);
	}
	
	public String getDataEntityAsFile(String inDataId) throws OcfaException {
		
		return getFile(getReference(inDataId)).getAbsolutePath();
		
	}
	
	protected String getReference(String inDataId) throws OcfaException{
		
		Connection connection = null;
		try {
			connection = pool.getConnection();
			PreparedStatement statement = 
				connection.prepareStatement("select * from evidencestoreentity where id = " + inDataId);
			ResultSet set = statement.executeQuery();
			if (set.next()){
				
				return set.getString("repname");
				
			} else {
				
				logger.error("unknown dataid " + inDataId);
				throw new OcfaException("unknwon dataid " + inDataId);
			}
		} catch(SQLException e){
			
			logger.error("getDataEntityAsStream: " + e.getMessage());
			throw new OcfaException(e.getMessage());
		} finally {
			
			if (connection != null){
				
				try {
					
					connection.close();
					
				} catch(Exception e){
					
					logger.error("getDataEntityAsStream: cannot close connection" + e.getMessage());
				}
			}
			
		}	
	}
	
	
	public InputStream getDataEntityAsStream(String inDataId) throws OcfaException{
		

				return getStream(getReference(inDataId));
	
	}
	
	/**
	 * Helpermethos that returns a stream to the evdiencestoreentity.
	 */
	protected InputStream getStream(String inReference) throws OcfaException {
		
		try {
			return new FileInputStream(getFile(inReference));
		} catch (FileNotFoundException e){
			
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		}
	}

	
	public void updateMetaStoreEntity(String inReference, String inNewContent) throws OcfaException {

		metaRepos.updateMetaStoreEntity(inReference, inNewContent);
	}
	
	/**
	 * Cretaes a new evidencestoreentity.
	 */
	public EvidenceStoreEntity createEvidenceStoreEntity(File inFile) throws OcfaException{
		
		File source = inFile;
		DigestPair digest;
		// if the mode is a copy mode, create a temporary file in the repository root while computing the 
		// digest. 
		if (mode == RepositoryMode.COPY){
			
			try {
				source = File.createTempFile("javaapi", "tmp", repositoryRoot);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new OcfaException("Cannot create tmp filesql in " + repositoryRoot);
			}
			digest = computeDigest(inFile, source);
		}else {
			
			digest = computeDigest(inFile);
		}
		return createEvidenceStoreEntity(source, digest);	
	}
	
	/**
	 * Helper method for creating evidencestoreentities. 
	 * @param inFile
	 * @param digest
	 * @return
	 * @throws OcfaException
	 */
	protected EvidenceStoreEntity createEvidenceStoreEntity(File inFile, DigestPair digest) throws OcfaException{
		
		EvidenceStoreEntity entity = new EvidenceStoreEntity();
		SplittedSha1 splitSha1 = splitSHA1(digest.getSha1());
		putIntoRepository(inFile, splitSha1);
		String dataId = putEvidenceStoreEntityIntoDb(splitSha1);
		entity.setHandle(dataId);
		entity.setPair(digest);
		return entity;

		
	}
	
	
	/**
	 * Creates an evidencestoreentity from a stream
	 */
	public EvidenceStoreEntity createEvidenceStoreEntity(InputStream inStream) throws OcfaException{
	
		
		File tmpFile;
		DigestPair digest;
		try {

			tmpFile = File.createTempFile("jaavapi", "tmp", repositoryRoot);
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException("Cannot create tmp filesql in " + repositoryRoot);
		}
		digest = computeDigest(inStream, tmpFile);
		return createEvidenceStoreEntity(tmpFile, digest);
	}
	
	/**
	 * puts the newly created evidencestoreentity into the database.
	 * 
	 * @param splitSha1 object containing the filepath of the evidencestoreentity.
	 * @return the id of the new evidencestoreentity.
	 * @throws OcfaException
	 */
	private String putEvidenceStoreEntityIntoDb(SplittedSha1 splitSha1) throws OcfaException {
	
		Connection connection = null;
		PreparedStatement statement;
		String currVal;
		try {
			
			connection = pool.getConnection();
			statement = connection.prepareStatement(INSERT_INTO_EVIDENCESTOREENTITY);
			statement.setString(1, splitSha1.filePath);
			statement.execute();
			Statement getCurVal = connection.createStatement();
			ResultSet set = getCurVal.executeQuery(GET_CURR_VALL);
			if (set.next()){
				
				currVal = set.getString("curval");
			} else {
				
				throw new OcfaException("cannot get currval for evidencestoreentity");
			}
			return currVal;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		} finally {
			
			DbUtil.closeConnection(connection);
		}
			
		
	}

	/**
	 * Inserts the metadata into the repository, also creates an entry in the metadatainfo table.
	 * @param inMeta the string describing the metadata around a file.
	 * @param inDataId the handle to the evidencestoreentity. 
	 */
	public String insertMeta(String inMeta, String inDataId, EvidenceIdentifier inIdentifier) throws OcfaException {
	
		String metaDataRef = metaRepos.insertMetaStoreEntity(inMeta);
		insertMetaDataInfo(metaDataRef, inDataId, inIdentifier);	
		return metaDataRef;
	}
	
	
	/**
	 * Inserts an entry in the metadatainfo that descibes the connection between dataid and metadataid. 
	 * @param inMeta the handle to the metadata of the evidence
	 * @param inDataId the handle to the data of the evidence.
	 * @param inIdentifier struct containing item, source, caseid, evidenceid.
	 * @throws OcfaException
	 */
	private void insertMetaDataInfo(String inMetaId, String inDataId,
			EvidenceIdentifier inIdentifier) throws OcfaException {
		
		Connection connection = null;
		int itemId = getSerialItemId(inIdentifier.getCaseName(), inIdentifier.getEvidenceSource(), 
				inIdentifier.getItemName());
		try {
			
			connection = pool.getConnection();
			PreparedStatement statement = connection.prepareStatement(INSERT_INTO_METADATAINFO);
			statement.setInt(1, Integer.parseInt(inMetaId));
			if (inDataId != null && inDataId.length() > 0){
				
				statement.setInt(2, Integer.parseInt(inDataId));
			} else {
				
				statement.setNull(2, java.sql.Types.INTEGER);
			}
			
			statement.setString(3, inIdentifier.getEvidenceID());
			statement.setInt(4, itemId);			
			statement.execute();
			
		} catch(SQLException e){
			
			logger.error(e.getMessage());
		} finally {
			
			DbUtil.closeConnection(connection) ;
		}
		
		
	}

	/**
	 * Gets the item id of an item.
	 * @param caseName 
	 * @param evidenceSource
	 * @param itemName
	 * @return
	 * @throws OcfaException
	 */
	private int getSerialItemId(String caseName, String evidenceSource,
			String itemName) throws OcfaException {
		Connection connection = null;
		try {
			
			connection =  pool.getConnection();
			PreparedStatement statement = connection.prepareStatement(GET_ITEM_ID);
			statement.setString(1,caseName);
			statement.setString(2, evidenceSource);
			statement.setString(3, itemName);
			ResultSet set = statement.executeQuery();
			if (set.next()){
				
				return set.getInt(1);
			} else {
				
				throw new OcfaException("cannot get itemid for " + caseName + ", " 
						+ evidenceSource + ", " + itemName);				
			}
			
			
			
		} catch(SQLException e){
			
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new OcfaException(e.getMessage());
		} finally {
			
			DbUtil.closeConnection(connection);
		}
		
	}

	/**
	 * puts inSource into the repository. 
	 * @param inSource the file that is moved into the repository.
	 * @param inReposInfo information about the future location of inSource in the repositoyr.
	 * @throws OcfaException
	 */
	protected void putIntoRepository(File inSource, SplittedSha1 inReposInfo) throws OcfaException{
		
		File destination = new File(repositoryRoot, inReposInfo.filePath);
		if (!destination.exists()){
			
				destination.getParentFile().mkdirs();
				if (!inSource.renameTo(destination)){
					
					// we could not move the source to destination, throw exception unless we have a
					// race condition.
					if (!destination.exists()){
						
						throw new OcfaException("cannot rename " + inSource.getAbsolutePath() + " to " + destination.getAbsolutePath());
					}
			}
		} 
		// clean up. If the source still exists (is not moved), destroy it.
		if (inSource.exists()){
			
			if (!inSource.delete()){
				
				logger.error("cannot delete " + inSource.getAbsolutePath());
			}
		}
	}
	
	/**
	 * convenience methods around
	 * public static DigestPair computeDigest(InputStream inStream, File outputFile)
	 * @param inFile
	 * @return
	 * @throws OcfaException
	 */
	public static DigestPair computeDigest(File inFile) throws OcfaException{
		
		return computeDigest(inFile, null);
		
	}
	
	public static DigestPair computeDigest(File inFile, File outputFile) throws OcfaException{
	
		FileInputStream inStream;
		try {
			inStream = new FileInputStream(inFile);
			return computeDigest(inStream, outputFile);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		}
		
	}
	/**
	 * Computes a digest (and if necessary copies a file to a new place.);
	 * @param inFile
	 * @param ioOutputFile a file to which inFile should be copied.
	 * @return digest a pair of md5 and sha1
	 * @throws OcfaException
	 */
	public static DigestPair computeDigest(InputStream inStream, File outputFile) throws OcfaException{
		
		BufferedOutputStream ostream = null;
		try {
			
			DigestPair pair = new DigestPair();
			MessageDigest sha1 = MessageDigest.getInstance("SHA");
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			try {
				if (outputFile != null){
				
					ostream = new BufferedOutputStream(new FileOutputStream(outputFile));
				} 
				
				BufferedInputStream inputStream =
						new BufferedInputStream(inStream);
				byte [] buffer = new byte[2048];
				int length = 0;
				while ((length = inputStream.read(buffer))  > 0){					
					sha1.update(buffer, 0, length);	
					md5.update(buffer, 0, length);
					if (ostream != null){
						
						ostream.write(buffer, 0, length);
					}
				}
				pair.setMd5(convertToHex(md5.digest()));
				pair.setSha1(convertToHex(sha1.digest()));
				return pair;
				 
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new OcfaException(e.getMessage());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new OcfaException(e.getMessage());
			}
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new OcfaException(e.getMessage());
		} finally {
			
			if (ostream != null){
				
				try {
					ostream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method that convert some data into hexadecimal.
	 * @param data
	 * @return
	 */
	private static String convertToHex(byte[] data) {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < data.length; i++){
			
			int halfByte = (data[i] >>> 4) & 0x0F;
			int twoHalfs = 0;
			do {
				
					if ((0 <= halfByte) && (halfByte <= 9)){
			
						buffer.append(((char)('0' + halfByte)));
					} else {
						
						buffer.append((char)('a' + (halfByte - 10)));
					}
					halfByte = data[i] & 0x0F;
			} while (twoHalfs++ < 1);
		}
		return buffer.toString();
			
	}
		
	
	   // split SHA1 into dirs and filename
    // 
    // The path returned is relative to d_root
    static public SplittedSha1 splitSHA1(String inSha1) 
    	throws OcfaException{

      if (inSha1.length() != 40){
    	  throw new OcfaException("Not a valid SHA1 hash");
      }

      // parts is used as a template how we want the string to be split up;
      // in this case 2 chars / 2 chars / <rest of string (denoted by -1)>
      int parts[] = {2,2,-1};
      ArrayList<String> dirs = new ArrayList<String>();
      StringBuffer path = new StringBuffer("/");
      // first the dirs ...
      int partIndex;
      int currentPosition;
      for (partIndex = 0, currentPosition = 0; parts[partIndex] > 0; partIndex++){
    	  
    	  dirs.add(inSha1.substring(currentPosition, currentPosition +  parts[partIndex]));
    	  path.append(dirs.get(partIndex));
    	  path.append("/");
    	  currentPosition += parts[partIndex];
      }
  
      String filename = inSha1.substring(currentPosition, inSha1.length());
      SplittedSha1 returnValue= new SplittedSha1();
      returnValue.dirs = dirs;
      returnValue.filePath = path.toString() + filename;
      returnValue.fileName = filename;
      return returnValue;
    }

    public void createItem(String inCaseName, String inSource, String inItem) throws OcfaException{
    	
    	Connection connection = pool.getConnection();
    	try {
    		
    		PreparedStatement statement = connection.prepareStatement(CREATE_ITEM);
    		statement.setString(1, inCaseName);
    		statement.setString(2, inSource);
    		statement.setString(3, inItem);
    		statement.execute();
    	} catch(SQLException e){
    		
    		logger.error("createItem: error creating " + inItem  + ": " + e.getMessage());
    		throw new OcfaException(e.getMessage());
    	} finally {
    		
    		DbUtil.closeConnection(connection);
    	}
    }
    
    
    public static class SplittedSha1 {
    	
    	public ArrayList<String> dirs;
    	public String filePath;
    	public String fileName;
    }


	public RepositoryMode getMode() {
		return mode;
	}

	public void setMode(RepositoryMode mode) {
		this.mode = mode;
	}
	
	
}
