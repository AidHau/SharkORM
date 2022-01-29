package com.revature.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.revature.annotations.Column;
import com.revature.annotations.PrimaryKey;
import com.revature.models.Model;

public class CRUD {
	private static LinkedList<String> transaction = new LinkedList<String>();
	private Connection conn;
	private static Queries qBuild = new Queries();
	private boolean autoCommit = true;

	public CRUD(Connection conn, boolean autoCommit) {
		this.conn = conn;
		
		this.autoCommit = autoCommit;
	}
	
	public boolean getAutoCommit() {
		return this.autoCommit;
	}
	
	public boolean setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
		return this.autoCommit;
	}
	
	public Optional<List<Object>> findAll(Class<?> clazz) {
		try {
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			Model<Class<?>> metaModel = Model.of(clazz);
			String sql = qBuild.findAllQuery(metaModel);

			ResultSet rs;

			if ((rs = stmt.executeQuery(sql)) != null) {
//				ResultSetMetaData rsmd = rs.getMetaData();
//				int columnCount = rsmd.getColumnCount();

				while (rs.next()) {
					Object newObject = clazz.getConstructor().newInstance();
					for (Field field : fields) {
						Column col = field.getAnnotation(Column.class);
						PrimaryKey id = field.getAnnotation(PrimaryKey.class);
//						JoinColumn fk = field.getAnnotation(JoinColumn.class);
						String name;

						if (id != null) {
							name = id.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}

						if (col != null) {
							name = col.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}

//						if (fk != null) {
//							name = fk.name();
//							if (rs.getObject(name) != null) {
//								Object value = rs.getObject(name);
//								field.set(newObject, value);
//							}
//						}
					}

					list.add(newObject);

				}

			}
			return Optional.of(list);

		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (SecurityException e) {
			e.printStackTrace();
			return Optional.empty();
		}

	}

	public Optional<List<Object>> findBy(Class<?> clazz, String columns, String conditions) {
		try {
			List<String> cols = Arrays.asList(columns.split("\\s*,\\s*"));
			List<String> cons = Arrays.asList(conditions.split("\\s*,\\s*"));
			List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
			for (Field field : fields) {
				field.setAccessible(true);
			}
			List<Object> list = new ArrayList<>();
			Statement stmt = conn.createStatement();
			Model<Class<?>> metaModel = Model.of(clazz);
			String sql = qBuild.findByQuery(metaModel);
			List<String> query = new ArrayList<>();
			for (int i = 0; i < cols.size(); i++) {
				if (cols.get(i).equals("id")) {
					query.add(cols.get(i) + " = " + cons.get(i));
				} else if (metaModel.getColumn(cols.get(i)) != null
						&& metaModel.getColumn(cols.get(i)).getType() == String.class) {
					query.add(cols.get(i) + " = '" + cons.get(i) + "'");
				} else {
					query.add(cols.get(i) + " = " + cons.get(i));
				}
			}
			sql += query.stream().collect(Collectors.joining(" AND ")) + (";");

			ResultSet rs;

			if ((rs = stmt.executeQuery(sql)) != null) {
				ResultSetMetaData rsmd = rs.getMetaData();

				while (rs.next()) {
					Object newObject = clazz.getConstructor().newInstance();
					for (Field field : fields) {
						Column col = field.getAnnotation(Column.class);
						PrimaryKey id = field.getAnnotation(PrimaryKey.class);
//						JoinColumn fk = field.getAnnotation(JoinColumn.class);
						String name;

						if (id != null) {
							name = id.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}
						if (col != null) {
							name = col.name();
							if (rs.getObject(name) != null) {
								Object value = rs.getObject(name);
								field.set(newObject, value);
							}
						}
//						if (fk != null) {
//							name = fk.name();
//							if (rs.getObject(name) != null) {
//								Object value = rs.getObject(name);
//								field.set(newObject, value);
//							}
//						}
					}

					list.add(newObject);

				}

			}
			return Optional.of(list);

		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return Optional.empty();
		} catch (SecurityException e) {
			e.printStackTrace();
			return Optional.empty();
		}

	}

	public boolean remove(Object model) {
		try {
			Model<Class<?>> metaModel = Model.of(model.getClass());
			String sql = qBuild.deleteQuery(metaModel);
			sql += metaModel.getPrimaryKey().getColumnName() + " = " + metaModel.getPrimaryKey().getValue(model) + ";";

			if (!autoCommit) {
				transaction.add(sql);
				return true;
			} else {

				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;

			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean save(Object model) {
		Model<Class<?>> metaModel = Model.of(model.getClass());
		String sql = qBuild.saveQuery(metaModel);
		List<String> sqlUpdate = new ArrayList<String>();
		for (String string : metaModel.getColumnNameListNoFk()) {
			try {
				if (metaModel.getColumn(string) != null) {
					if (metaModel.getColumn(string) != null
							&& metaModel.getColumn(string).getType().equals(String.class)) {
						sqlUpdate.add("'" + metaModel.getColumn(string).getValue(model) + "'");
					} else {
						sqlUpdate.add((String) metaModel.getColumn(string).getValue(model));
					}
				}

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return false;
			}
		}

		sql += sqlUpdate.stream().collect(Collectors.joining(" , ")) + ") ON CONFLICT DO NOTHING;";
		if (!autoCommit) {
			transaction.add(sql);
			return true;
		} else {
			
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			
		}

	}

	public boolean update(Object model, String columns) {
		Model<Class<?>> umodel = Model.of(umodel.getClass());
		String[] desiredColumns = columns.split("\\s*,\\s*");
		String sql = qBuild.updateQuery(umodel);
		
			List<String> sqlUpdate = new ArrayList<String>();
			for (String string : desiredColumns) {
				try {
					if (umodel.getColumn(string) != null) {
						if (umodel.getColumn(string).getType().equals(String.class)) {
							sqlUpdate.add(string + " = '" + umodel.getColumn(string).getValue(umodel) + "'");
						} else {
							sqlUpdate.add(string + " = " + umodel.getColumn(string).getValue(umodel));
						}
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return false;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return false;
				}
			}
			
			sql += sqlUpdate.stream().collect(Collectors.joining(",\r\n")) + " \r\n";
			
			try {
				sql += "WHERE " + umodel.getPrimaryKey().getColumnName() + " = "
						+ umodel.getPrimaryKey().getValue(umodel);
				sql += ";";
				System.out.println(sql);
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				return false;
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				return false;
			}
		if (!autoCommit){
			transaction.add(sql);
			return true;
		} else {
			
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}


	}
	
	public void beginTransaction() {
		transaction = new LinkedList<String>();
		transaction.add("BEGIN;");
	}
	
	public boolean persistMetaModel(Model<Class<?>> metaModel) {
		if (metaModel != null) {			
			String sql = qBuild.createTableIfNotExists(metaModel);
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;

	}
}
