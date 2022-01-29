package com.revature.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.revature.annotations.Entity;
import com.revature.persistence.CRUD;
import com.revature.models.Model;
import com.revature.persistence.Queries;

public class Config {
	
	private static HashMap<Class<?>, HashSet<Object>> cache;
	private static Queries qBuilder = new Queries();
	private static CRUD CRUD;
	private static ConnectionPool cPool = new ConnectionPool();
	private static ClassFinder cFinder = new ClassFinder();
	private static Connection conn;
	private static List<Model<Class<?>>> metaModelList;
	private static String PACKAGE_NAME;
	private static Properties props = new Properties();
	private boolean autoCreateTables = true;
	private boolean autoCommit = true;
	
	static {
		try {
			props.load(new FileReader("src\\main\\resources\\application.properties"));
			PACKAGE_NAME = props.getProperty("packageName");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Config() {
		cache = new HashMap<Class<?>, HashSet<Object>>();
		addAllMetaModels();
		try {
			conn = getConnection();
			CRUD = new CRUD(conn, autoCommit);
			this.autoCommit = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		persistMetaModels();
		
	}	
	public Config(boolean autoCommit) {
		cache = new HashMap<Class<?>, HashSet<Object>>();
		addAllMetaModels();
		try {
			conn = getConnection();
			CRUD = new CRUD(conn, autoCommit);
			setAutoCommit(autoCommit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		persistMetaModels();
		
	}	
	
	public boolean setAutoCommit(boolean autoCommit) {
		CRUD.setAutoCommit(autoCommit);
		if (!CRUD.getAutoCommit()) {
			CRUD.beginTransaction();
		}
		this.autoCommit = autoCommit;
		return this.autoCommit;
	}
	
	public boolean disableAutoCommit() {
		CRUD.setAutoCommit(false);
		CRUD.beginTransaction();
		this.autoCommit = false;
		return this.autoCommit;
	}
	
	public boolean getAutoCommit() {
		return this.autoCommit;
	}
	
	public Connection getConnection() throws SQLException {
		DataSource dSource = cPool.setUpPool();
		
		cPool.printDbStatus();
		System.out.println("======Connecting to the DB=======");
		
		return dSource.getConnection();
	}
	
	public Config addAnnotatedClass(Class<?> annotatedClass) {
		metaModelList.add(Model.of(annotatedClass));
		return this;
	}
	
	public List<Model<Class<?>>> getMetaModels() {
		
		return metaModelList;
		
	}
	
	public Model<Class<?>> getMetaModel(Class<?> desired) {
		Optional<Model<Class<?>>> metaModel;
		
		metaModel = metaModelList.stream().filter( m -> m.getSimpleClassName().equals(desired.getSimpleName()))
										  .findFirst();
		
		return metaModel.isPresent() ? metaModel.get() : null;
	}
	
	public List<Model<Class<?>>> addAllMetaModels() {
		if (metaModelList == null) {
			metaModelList = new LinkedList<Model<Class<?>>>();
		}
		
		Set<Class<?>> packageClasses = cFinder.findAllClasses(PACKAGE_NAME);
		packageClasses.stream()
						.filter(c -> c.getAnnotation(Entity.class) != null)
						.forEach(c -> addAnnotatedClass(c));
		 return metaModelList;
	}
	
	public void persistMetaModels() {
		
		for (Model<Class<?>> m : metaModelList) {
			CRUD.persistMetaModel(m);
		}
	
	}
	
	public HashMap<Class<?>, HashSet<Object>> getCache() {
		return cache;
	}

	public boolean updateObjectInDB(final Object obj,final String update_columns) {
		
		return CRUD.update(obj, update_columns);
	}
	
	public boolean removeObjectFromDB(final Object obj) {
		return CRUD.remove(obj);
	}
	
	public boolean addObjectToDB(final Object obj) {
		return CRUD.save(obj);
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz) {
		if (cache.get(clazz) != null && !cache.get(clazz).isEmpty()) {
			return Optional.of(cache.get(clazz).stream().collect(Collectors.toList()));
		} else {
			return CRUD.findAll(clazz);
		}
	}
	
	public Optional<List<Object>> getListObjectFromDB(final Class<?> clazz, String columns, String conditions) {
//		if (!cache.get(clazz).isEmpty() && cache.get(clazz) != null) {
//			MetaModel<Class<?>> metaModel = getMetaModel(clazz);
//			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
//			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
//			Set<Object> allObjects = cache.get(clazz);
//			
//			for (int i = 0; i < cols.size(); i++) {
//				
//			}
//			
//			List<Object> requested;
//			
//			
//			return Optional.of(null);
//		}
		return CRUD.findBy(clazz, columns, conditions);
	}
	
	public void addAllFromDbToCache(Class<?> clazz) {
		List<Object> list = !getListObjectFromDB(clazz).isPresent() ? getListObjectFromDB(clazz).get() : null;
		cache.put(clazz, (HashSet<Object>) list.stream().collect(Collectors.toSet()));
	}
	
	public void rollBackTransaction() {
		if (!autoCommit) {
			CRUD.rollback();
			CRUD.beginTransaction();
		} 
	}
	
	public void rollBackToSavePoint(String savePoint) {
		if (!autoCommit) {
			CRUD.rollback(savePoint);
		} 
	}
	
	public void releaseSavePoint(String savePoint) {
		if (!autoCommit) {
			CRUD.releaseSavePoint(savePoint);
		} 
	}
	
	public void createSavePoint(String savePoint) {
		if (!autoCommit) {
			CRUD.setSavePoint(savePoint);
		} 
	}
	
	public void commitTransaction() {
		if (!autoCommit) {
			CRUD.sendCommit();
		} 
	}
	
}