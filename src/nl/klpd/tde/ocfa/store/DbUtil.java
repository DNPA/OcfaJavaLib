package nl.klpd.tde.ocfa.store;

import java.sql.Connection;
import java.sql.SQLException;

import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * DbUtil
 * 
 * Helper methods for qorking with the database
 * @author joep
 *
 */
public class DbUtil {

	static public void closeConnection(Connection inConnection) throws OcfaException{
		
		if (inConnection != null){
			
			try {
				//logger.debug("getMetaAsString: closing connection");
				inConnection.close();
			} catch (SQLException e) {
				//
				//logger.error("getMetaAsString: error closing connection " + e.getMessage() );
				throw new OcfaException(e.getMessage());
			}
			
		}
		
	}
	
}
