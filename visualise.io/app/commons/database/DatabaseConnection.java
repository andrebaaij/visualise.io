package commons.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

public class DatabaseConnection {

	private Connection connection;
	private String databaseName;

	private Integer generatedKey, affectedRows;
	public static final int SELECT = 0, INSERT = 1, UPDATE = 2, DELETE = 3;
	
	private DataSource datasource;

	public DatabaseConnection(DataSource datasource) throws Exception {
		this.datasource = datasource;
	}

	/**
	 * @return the generatedKey
	 */
	public Integer getGeneratedKey() {
		return generatedKey;
	}

	/**
	 * @param generatedKey
	 *            the generatedKey to set
	 */
	private void setGeneratedKey(Integer generatedKey) {
		this.generatedKey = generatedKey;
	}

	private void connect() {
		int tries = 3;
		for (int i = 0; i < tries; i++) {
			try {
				//Connect to the database.
				this.connection = datasource.getConnection();
				break;
			}
			// Handle any errors that may have occurred.
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void disconnect() {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<ArrayList<Object>> executeQuery(int executeType, String statement, Object... parameters) {
		// Connect to the database
		connect();
		
		// Prepare all parameters, if the parameter is an array, the first dimension is read out.
		ArrayList<Object> parameterList = new ArrayList<Object>(); 
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] == null) {
				parameterList.add(parameters[i]);
			}
			else if (parameters[i].getClass().isArray()) {
				Object[] objectArray = (Object[]) parameters[i];
				for (int j = 0; j < objectArray.length; j++) {
					parameterList.add(objectArray[j]);
				}
			}
			else {
				parameterList.add(parameters[i]);
			}
		}

		PreparedStatement SQLStatement = null;
		ResultSet resultSet = null;
		
		try {
			SQLStatement = connection.prepareStatement(statement,Statement.RETURN_GENERATED_KEYS);
			parameters = parameterList.toArray();

			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] == null) {
					SQLStatement.setNull(i + 1, java.sql.Types.JAVA_OBJECT);
				} else if (parameters[i] instanceof Integer) {
					SQLStatement.setInt(i + 1, (Integer) parameters[i]);
				} else if (parameters[i] instanceof String) {
					SQLStatement.setString(i + 1, (String) parameters[i]);
				} else if (parameters[i] instanceof Date) {
					SQLStatement.setDate(i + 1, new java.sql.Date(((Date) parameters[i]).getTime()));
				} else if (parameters[i] instanceof Boolean) {
					SQLStatement.setBoolean(i + 1, (Boolean) parameters[i]);
				} else {
					System.err.format("Unsupported parameter instance (%s)!", parameters[i].getClass().toString());
				}
			}

			try {
				if (executeType == INSERT || executeType == UPDATE || executeType == DELETE) {
					int result = SQLStatement.executeUpdate();
					setAffectedRows(result);
					if (result == 0) {
						// TODO generate message based on executeType
						// if (insert) than nothing inserted
						// if (update) than nothing updated
						// if (delete) than nothing deleted
					} else {
						ResultSet keys = SQLStatement.getGeneratedKeys();
						if (keys != null && executeType == INSERT) {
							// executeType added because there is only a key
							// generated in case of an Insert statement
							keys.next();
							// below we use 1 because we currently assume that
							// only 1 row is updated, inserted or deleted
							setGeneratedKey(keys.getInt(1));
						}
					}
					return null;
				} else if (executeType == SELECT) {
					resultSet = SQLStatement.executeQuery();

					if (resultSet != null) {
						ArrayList<ArrayList<Object>> table;
						int columnCount = resultSet.getMetaData().getColumnCount();
	
						ArrayList<Object> header = new ArrayList<Object>(
								columnCount);
						for (int c = 1; c <= columnCount; ++c) {
							header.add(resultSet.getMetaData().getColumnName(c));
						}
	
						if (resultSet.getType() == ResultSet.TYPE_FORWARD_ONLY)
							table = new ArrayList<ArrayList<Object>>();
						else {
							resultSet.last();
							table = new ArrayList<ArrayList<Object>>(
									resultSet.getRow() + 1);
							resultSet.beforeFirst();
						}
						table.add(header);
	
						for (ArrayList<Object> row; resultSet.next(); table
								.add(row)) {
							row = new ArrayList<Object>(columnCount);
	
							for (int c = 1; c <= columnCount; ++c) {
	
								if (resultSet.getObject(c) != null) {
									if (resultSet.getMetaData()
											.getColumnClassName(c)
											.equalsIgnoreCase("java.lang.String")) {
										row.add(resultSet.getString(c));
									} else if (resultSet.getMetaData()
											.getColumnClassName(c)
											.equalsIgnoreCase("java.lang.Integer")) {
										row.add(resultSet.getInt(c));
									} else {
										row.add(resultSet.getObject(c));
									}
								} else {
									if (resultSet.getMetaData()
											.getColumnClassName(c)
											.equalsIgnoreCase("java.lang.String")) {
										row.add((String) null);
									} else if (resultSet.getMetaData()
											.getColumnClassName(c)
											.equalsIgnoreCase("java.lang.Integer")) {
										row.add((Integer) null);
									} else
										row.add(null);
	
								}
	
							}
						}
						return table;
					}
					else {
						return null;
					}
				} else
					return null;
			} catch (SQLException problem) {
				System.err.println(problem.getMessage());
				// executionProblem_ = problem.getMessage();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			disconnect();
			
			if (connection != null)
				try {
					connection.close();
					connection = null;
				} catch (Exception e) {
				}
			if (SQLStatement != null)
				try {
					SQLStatement.close();
					SQLStatement = null;
				} catch (Exception e) {
				}
			if (resultSet != null)
				try {
					resultSet.close();
					resultSet = null;
				} catch (Exception e) {
				}
		}
	}

	public Integer getAffectedRows() {
	   return affectedRows;
   }

	public void setAffectedRows(Integer affectedRows) {
	   this.affectedRows = affectedRows;
   }
}