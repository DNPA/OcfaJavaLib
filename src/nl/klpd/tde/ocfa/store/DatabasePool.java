package nl.klpd.tde.ocfa.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import nl.klpd.tde.ocfa.misc.OcfaConfig;
import nl.klpd.tde.ocfa.misc.OcfaException;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
/**
 * implementation around a pool of connections. Normally 
 * only 1 connection is used at the same time. However, it might be possible to use more (e.g. in the 
 * duif interface or omo).
 * @author joep
 * @todo betrer integrate this stuff with omo and duif.
 */
public class DatabasePool {

	private String url;
	private String name;
	private String password;
	private PoolingDataSource dataSource = null;
	private GenericObjectPool debugPool = null;
	private static Log logger = LogFactory.getLog(DatabasePool.class);
	public DatabasePool(OcfaConfig config) throws OcfaException {
		
	
		try {
			Class.forName("org.postgresql.Driver");

			createDataSource(config);
//			Class.forName("org.postgresql.Driver");
//
//			url = "jdbc:postgresql://" + properties.getProperty("storedbhost") 
//		    + "/" + properties.getProperty("storedbname");
//			name = properties.getProperty("storedbuser");
//			password = properties.getProperty("storedbpasswd", "");
			
			
		}  catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(OcfaException.Type.NO_CONNECTION_TO_DB, "cannot load postgres driver");
		} 
		
	}	
	private void createDataSource(OcfaConfig inConfig) {
		
		
		int maxConnections  = 1;
		
		
/**
 * @TODO allow configurable amounf of connections.
 * Somewhere we should check here whethere more connections are needed.
 * this would allow some modules, like the config tool or the user interface to have more than one
 * connection to the database.
 */
		logger.info("using " + maxConnections + " as maximum connections");
		GenericObjectPool connectionPool = new GenericObjectPool(null, maxConnections);
		debugPool = connectionPool;
		connectionPool.setTimeBetweenEvictionRunsMillis(600 * 1000);
		url = "jdbc:postgresql://" + inConfig.getProperty("storedbhost") 
		+ "/" + inConfig.getProperty("storedbname");
		name = inConfig.getProperty("storedbuser");
		password = inConfig.getProperty("storedbpasswd", "");
		logger.info("passwordje  is  " + password);
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, name, password);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
		dataSource = new PoolingDataSource(connectionPool);
		
	}

	public void setMaxConnections(int inMaxConnections){
		
		debugPool.setMaxActive(inMaxConnections);
	}
	
	public Connection getConnection() throws OcfaException {
		
			try {
				logger.debug("connections active is " + debugPool.getNumActive());
				logger.debug("connection idle is " + debugPool.getNumIdle());

				return dataSource.getConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("password is "+ password);
				logger.error("name is " + name);
				logger.error("url is " + url);
				throw new  OcfaException(OcfaException.Type.NO_CONNECTION_TO_DB, e.getMessage());
			}
			
	
	}

}
