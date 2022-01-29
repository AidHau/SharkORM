package com.revature.models;

import java.lang.reflect.Field;
import java.util.Objects;

import com.revature.annotations.Column;

public class SuperColumn {
	
	private Field field;
	
	// generate ColumnField by passing fields with @Column
	public SuperColumn(Field field) {
		
		if (field.getAnnotation(Column.class) == null) {
			throw new IllegalStateException("Cannot CreateColumnFIeld object! Provided field, " +
					  						getName() + "is not annotated with @Column");
		}
		this.field = field;
	}
	
	// get field
	public Field getField() {
		return this.field;
	}
	
	public Object getValue(Object obj) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		return field.get(obj);
	}
	
	// get field name
	public String getName() {
		return field.getName();
	}
	
	// get field type
	public Class<?> getType() {
		return field.getType();
	}
	
	// get column name from annotation. If blank, create snake case column name using field name
	public String getColumnName() {
		return !field.getAnnotation(Column.class).name().isBlank() ? field.getAnnotation(Column.class).name() : field.getName().replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase();
	}

	@Override
	public int hashCode() {
		return Objects.hash(field);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuperColumn other = (SuperColumn) obj;
		return Objects.equals(field, other.field);
	}
	
}