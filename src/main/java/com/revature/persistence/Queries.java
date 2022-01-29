package com.revature.persistence;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.revature.annotations.Column;
import com.revature.models.SuperColumn;
import com.revature.models.Model;
import com.revature.models.SuperKey;

public class Queries {
	
private static HashMap<String, String> dataTypes = new HashMap<String,String>();
	
	static {
		
		Scanner scan;
		try {
			scan = new Scanner(new File("src\\main\\resources\\dataTypes.csv"));
			while (scan.hasNextLine()) {
				String[] dataMapping = scan.nextLine().split(",");
				dataTypes.put(dataMapping[0],dataMapping[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method generates the SQL string for creating a table from an annotated class.
	 * @param model (which is generated from an annotated class)
	 * @return DDL String for creating a table.
	 */
	public String createTableIfNotExists(Model<Class<?>> model) {
		String newTableQuery = "";
		if (model != null) {
			// CREATE TABLE IF NOT EXISTS table (
			newTableQuery += "CREATE TABLE IF NOT EXISTS " + model.getTableName() + " (\r\n";
			// id SERIAL PRIMARY KEY,
			SuperKey pk = model.getPrimaryKey();
			newTableQuery += pk.getColumnName() + " SERIAL PRIMARY KEY";
			if (model.getColumns() != null && !model.getColumns().isEmpty()) {

				for (SuperColumn column : model.getColumns()) {
					newTableQuery += ",\r\n";
					// column DataTypeE NOT NULL UNIQUE,
					newTableQuery += column.getColumnName() + " " + dataType(column.getField());
					if (column.getField().getAnnotation(Column.class) != null
							&& column.getField().getAnnotation(Column.class).NotNull()) {
						newTableQuery += " NOT NULL";
					}
					if (column.getField().getAnnotation(Column.class) != null
							&& column.getField().getAnnotation(Column.class).unique()) {
						newTableQuery += " UNIQUE";
					}
				}
			}
			

			newTableQuery += "\r\n);";

		}
		return newTableQuery;
	}
	
	public String saveQuery(Model<Class<?>> metaModel) {
		String sql = "INSERT INTO ";
		// INSERT INTO table 
		sql += metaModel.getTableName() + " (";
		//(column 1, column 2, column 3, ...)
		List<String> columns = metaModel.getColumnNameListNoFk();
		sql += columns.stream().collect(Collectors.joining(", ")) + ") VALUES \r\n(";
		
		return sql;
	}
	
	public String updateQuery(Model<Class<?>> metaModel) {
		String sql = "UPDATE ";
		// UPDATE table_name
		sql += metaModel.getTableName() + "\r\nSET ";
//		// SET column1 = value, update 2 = value, ...
//		List<String> columns = metaModel.getColumnNameList();
//		sql += columns.stream().collect(Collectors.joining(" = ? , ")) + " = ?\r\n";
//		// WHERE
//		sql += "WHERE " + metaModel.getPrimaryKey().getColumnName() + " = ?;";
		
		return sql;
	}
	
	public String deleteQuery(Model<Class<?>> metaModel) {
		return "DELETE FROM " + metaModel.getTableName() + " WHERE ";
	}
	
	public String findAllQuery(Model<Class<?>> metaModel) {
		return "SELECT * FROM " + metaModel.getTableName() + ";";
	}
	public String findByQuery(Model<Class<?>> metaModel) {
		return "SELECT * FROM " + metaModel.getTableName() + " WHERE ";
	}
	
	/**
	 * Helper method for grabbing the dataType of the field
	 * @param field
	 * @return Data type
	 */
	public String dataType(Field field) {
		return dataTypes.get(field.getType().getSimpleName().toLowerCase());
	}
	
	public HashMap<String, String> getDataTypes() {
		return dataTypes;
	}
	
}
