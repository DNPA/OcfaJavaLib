package nl.klpd.tde.ocfa.store;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.klpd.tde.ocfa.misc.EvidenceIdentifier;
import nl.klpd.tde.ocfa.misc.OcfaException;
/**
 * implementation of the 21 repository as used.
 * @author joep
 *
 */
public class OcfaPgBlobMetaRepository implements MetaRepository {

	private static Log logger = LogFactory.getLog(OcfaPgBlobMetaRepository.class);

	public static String GET_XML =  "select content as xml from metastoreentity where id = cast(? as Integer)";
	
	
	public static String UPDATE_XML = "update metastoreentity set content = ? where id = cast(? as Integer)";

	public static String INSERT_XML = "insert into MetaStoreEntity (content) VALUES ( ? )";
	private static String GET_CURR_VALL = "select currval('metastoreentity_id_seq') as curval";

	private DatabasePool pool;
	
	public OcfaPgBlobMetaRepository(DatabasePool inPool){
		
		pool = inPool;
		
	}
	/**
	 * Returns the meta entity as a stream. 
	 * @param inReference metadataid  of the reference.
	 */
	public InputStream getMetaEntityAsStream(String inReference)
			throws OcfaException {
		
		byte[] bytes;
		try {
			bytes = getMetaAsString(inReference).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("cannot encode " + inReference);
			e.printStackTrace();
			throw new OcfaException("error converting " + inReference + "." + e.getMessage());
		}
		return new ByteArrayInputStream(bytes);
	}
	
	/**
	 * returns the metaentity as a string 
	 * @param inReferrence 
	 */
	public String getMetaAsString(String inReference) throws OcfaException {

		Connection connection = null;
		try {
			logger.debug("getMetasString getting connection");
			connection = pool.getConnection();
			PreparedStatement statement = connection.prepareStatement(GET_XML);
			statement.setString(1,inReference);
			ResultSet set = statement.executeQuery();
			if (set.next()){
				
				String metaData = set.getString("xml");
				//logger.info("returning " + metaData + " for " + inReference);
				return metaData;
			}
			else {
				
				throw new OcfaException("cannot find metadata with reference " + inReference);
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		} finally {
			
			DbUtil.closeConnection(connection);
			connection = null;
		}
		


	}
	
	/**
	 * updates a metastoreentity with new content
	 * @Param inReference the metadataid of the metastoreentity that should be updated.
	 * @param inNewContent the new content of the metastoreentity.
	 */
	public void updateMetaStoreEntity(String inReference, String inNewContent) throws OcfaException{
		
		Connection connection = null;
		try {
		//	logger.info("getting connection");
			connection = pool.getConnection();
	//		logger.info("preparing statement");
			PreparedStatement statement = connection.prepareStatement(UPDATE_XML);
	//		logger.info("prepared");
			statement.setString(1, inNewContent);
			statement.setString(2,inReference);
		//	logger.info("executing");
			statement.execute();
		//	logger.info("done");
		} catch(SQLException e){
			
			logger.error("Sqlexception " + e.getMessage());
			throw new OcfaException(e.getMessage());
			
		} finally {
			
			DbUtil.closeConnection(connection);
			connection = null;
		}

	}



	/**
	 * creates a new metastoreentity by inserting the string into the database.
	 * @param inMeta the content of the metastoreentity.
	 * @return the metadataid of the new content.
	 */
	public String insertMetaStoreEntity(String inMeta) throws OcfaException{
		
		Connection connection = null;
		try {
			String metaDataRef;
			connection = pool.getConnection();
			PreparedStatement statement = connection.prepareStatement(INSERT_XML);
			statement.setString(1, inMeta);
			statement.execute();
			Statement currValStatement = connection.createStatement();
			ResultSet set = currValStatement.executeQuery(GET_CURR_VALL);
			if (set.next()){
				
				metaDataRef = set.getString(1);
				return metaDataRef;
			} else {
				
				throw new OcfaException("cannot find currval");
			}
			
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		} finally {
			
			DbUtil.closeConnection(connection);
		}
	
		
	}
	

}
