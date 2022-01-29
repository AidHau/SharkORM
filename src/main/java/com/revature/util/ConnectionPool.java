package com.revature.util;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * COnnection Pooling means that connections are REUSED rather than created
 * each time a connection is requested.
 * 
 * In order to facilitate connection reuse, a memory cache of database connections
 * called a CONNECTION POOL, is maintained by a connection pooling module 
 * as a layer on top of any standard JDBC driver product.
 * 
 *
 */


// think about this as our ConnectionUtil.java...but on steroids.....
// we need to use a special library that gives the ability to create a POOL of connections, so that we can 
// perform multiple operations on the database at once 
public class ConnectionPool {
	private static Properties prop = new Properties();
	private static String JDBC_DRIVER;
	private static String JDBC_DB_URL;
	private static String JDBC_USER;
	private static String JDBC_PASS;
	
	static
	{
		try {
			prop.load(new FileReader("src\\main\\resources\\application.properties"));
			JDBC_DRIVER = prop.getProperty("jdbcDriver");
			JDBC_DB_URL = prop.getProperty("url");
			JDBC_USER = prop.getProperty("username");
			JDBC_PASS = prop.getProperty("password");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * We will use this class to supply general database credentials and attain 
	 * an object called GenericObjectPool
	 * 
	 * gPool is a special object that holds all the connections to our databaase at once
	 * 
	 * Having a connection pool drastically increases performance whenever  we perform a CRUD operation
	 * on the Database 
	 */
	
	private static GenericObjectPool gPool = null;
	
	// Apache Cmmons dbcp gives us the functionality to create a connection pool.  But we have to do so
	// by using its specific class and functionality called GenericObjectPool.
	
	public DataSource setUpPool() {
	// We use the DataSource Interface to create a connection object that participates in Connection Pooling
		
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		// create an instance of the GenericObjectPOol that holds our Pool of connection objects
		gPool = new GenericObjectPool();
		gPool.setMaxActive(5);
		
		// Create a connectionFacotry object which will be used by the pool object to create the connections (which are all objects)
		
		ConnectionFactory cf = new DriverManagerConnectionFactory(JDBC_DB_URL, JDBC_USER, JDBC_PASS);
		
		// Create a PoolableConnectionFactory that will wrap around the Connection
		// Object created by the above connectionFactory
		// in order to add pooling functionality.
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
		
		return new PoolingDataSource(gPool);
	}
	
	
	public GenericObjectPool getConnectionPool() {
		return gPool;
	}
	
	// for our own benefitlet's create a method to print the connection pool status
	public void printDbStatus() {
		
		System.out.println("Max: " + getConnectionPool().getMaxActive() + "; Active: " + getConnectionPool().getNumActive() +
				"; Idle: " + getConnectionPool().getNumIdle());
	}
	
	
	
	
	
	

}