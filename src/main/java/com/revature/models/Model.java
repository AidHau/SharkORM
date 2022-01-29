package com.revature.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.revature.annotations.Column;
import com.revature.annotations.Entity;
import com.revature.annotations.PrimaryKey;

public class Model<T> {
	
	private Class<?> clazz;
	private SuperKey superKey;
	private List<SuperColumn> superColumn;
	
	// Constructor for MetaModel. Only called via MetaModel.of(clazz).
	public Model(Class<?> clazz) {
		this.clazz = clazz;
		this.superKey = setPrimaryKey();
		this.superColumn = setColumns();
	}
	
	// Create MetaModel out of a class
	public static Model<Class<?>> of(Class<?> clazz) {
		
		// we check that the class we're passing through has the @Entity annotation
		if (clazz.getAnnotation(Entity.class) == null) {
			throw new IllegalStateException("Cannot create MetaModel object! Provided class "
					+ clazz.getName() + " is not annotated with @Entity");
			
		}
		// if so....return a new MetaModel object of the class passed through 
		return new Model<>(clazz);
	}
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and return the first field annotated with @Id
	 *  @return the primary key field
	 */
	public SuperKey setPrimaryKey() {
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
			
			if (pk != null) {
				SuperKey superKey = new SuperKey(field);
				return superKey;
			}
			
		}
		
		throw new RuntimeException ("No Primary Key found in " + clazz.getName());
	}
	
	public SuperKey getPrimaryKey() {
		return superKey;
	}
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and add all fields annotated with @Column to the columnFields attribute
	 *  @return the list of all of the ColumnFields
	 */
	public List<SuperColumn> setColumns() {
		if (superColumn == null) {
			superColumn = new LinkedList<SuperColumn>();
		}
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			
			if (column != null) {
				superColumn.add(new SuperColumn(field));
			}
			
		}
		
		if (superColumn.isEmpty()) {
			throw new RuntimeException ("No columns found in: " + clazz.getName());
		}
		
		return superColumn;
	}
	
	public List<SuperColumn> getColumns() {
		return superColumn;
	}
	
	public SuperColumn getColumn(String columnName) {
		Optional<SuperColumn> maybe = getColumns().stream()
												  .filter( m -> m.getColumnName().equals(columnName))
												  .findFirst();
		return maybe.isPresent() ? maybe.get() : null;
	}
	
	
	
	/**
	 *  This method will iterate through all of the clazz's declared fields
	 *  and add all fields annotated with @JoinColumn to the columnFields attribute
	 *  @return the list of all the ForeignKeyFields
	 */
	
	public List<String> getColumnNameList() {
		List<String> columns = getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList());
		
		return columns;
	}
	public List<String> getColumnNameListNoFk() {
		List<String> columns = getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList());
		
		return columns;
	}
	
	public List<String> getColumnNameListWithId() {
		List<String> columns = new ArrayList<String>();
		columns.add(getPrimaryKey().getColumnName());
		columns.addAll(getColumns().stream().map(c-> c.getColumnName()).collect(Collectors.toList()));
		
		return columns;
	}
	
	public Class<?> getClazz() {
		return this.clazz;
	}
	
	public String getSimpleClassName() {
		return clazz.getSimpleName();
	}

	public String getClassName() {
		return clazz.getName();
	}

	public String getTableName() {
		return !this.clazz.getAnnotation(Entity.class).name().isBlank() ? this.clazz.getAnnotation(Entity.class).name() : this.getSimpleClassName().replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase() + "s" ;
	}
	
}