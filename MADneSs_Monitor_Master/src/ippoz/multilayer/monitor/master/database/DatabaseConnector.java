/**
 * 
 */
package ippoz.multilayer.monitor.master.database;

import ippoz.madness.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.ibatis.common.jdbc.ScriptRunner;

/**
 * @author Tommy
 *
 */
public class DatabaseConnector {
	
	private Connection conn;
	private boolean alive;
	
	public DatabaseConnector(String dbName, String username, String password, boolean create){
		this("jdbc:mysql://localhost:3306/", dbName, "com.mysql.jdbc.Driver", username, password, create);
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public DatabaseConnector(String url, String dbName, String driver, String username, String password, boolean create){
		try {
			alive = false;
			if(create){
				dbName = "experiment";
				createDatabase(url, dbName, username, password);
			}
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url + dbName, username, password);
			alive = true;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			AppLogger.logError(getClass(), "MySQLError", "Impossible to estalish DB connection");
		} 
	}
	
	public Connection getConnection(){
		return conn;
	}
	
	public boolean update(String query){
		Statement stmt;
		try {
			stmt = conn.createStatement();
			//AppLogger.logInfo(getClass(), "QUERY: " + query);
			stmt.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			AppLogger.logException(getClass(), e, "Query not executed: '" + query + "'");
		}
		return false;
	}
	
	protected ArrayList<HashMap<String, String>> executeQuery(String query){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			//AppLogger.logInfo(getClass(), "QUERY: " + query);
			rs = stmt.executeQuery(query);
			return parseResultSet(rs);
		} catch (SQLException e) {
			AppLogger.logException(getClass(), e, "Query not executed: '" + query + "'");
		}
		return null;
	}
	
	public void closeConnection() throws SQLException{
		conn.close();
	}
	
	public ArrayList<HashMap<String, String>> executeCustomQuery(String[] params, String query){
		int i = 0;
		while(query.indexOf("?") != -1){
			if(params.length <= i){
				AppLogger.logError(DatabaseConnector.class, "SQLError", "Malformed parameters list");
			} else query = query.substring(0, query.indexOf("?")) + params[i] + query.substring(query.indexOf("?")+1);
			i++;
		}
		return executeQuery(query);
	}
	
	public ArrayList<HashMap<String, String>> executeBuildedQuery(String[] params, String[] tables, String filter, String[] orders){
		return executeQuery(buildQuery(params, tables, filter, orders));
	}
	
	public static String getFirstValueByTag(ArrayList<HashMap<String, String>> list, String tag){
		if(list.size() > 0)
			return list.get(0).get(tag);
		else return null;
	}
	
	public static LinkedList<String> filterOutput(ArrayList<HashMap<String, String>> result, String field){
		LinkedList<String> list = new LinkedList<String>();
		if(result.get(0).keySet().contains(field)) {
			for(HashMap<String, String> current : result){
				list.add(current.get(field));
			}
		}
		return list;
	}
	
	protected static ArrayList<HashMap<String, String>> parseResultSet(ResultSet rs){
		HashMap<String, String> partial;
		ArrayList<HashMap<String, String>> list = null;
		try {
			list = new ArrayList<HashMap<String, String>>();
			rs.beforeFirst();
			while(rs.next()){
				partial = new HashMap<String, String>();
				for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
					partial.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
				}
				list.add(partial);
			}
		} catch (SQLException e) {
			AppLogger.logException(DatabaseConnector.class, e, "Unable to read ResultSet");
			e.printStackTrace();
		}
		return list;
	}
	
	public static String buildQuery(String[] params, String[] tables, String filter, String[] orders){
		String finalQuery = "";
		if(params == null || params.length == 0) {
			finalQuery = "select *";
		} else {
			finalQuery = "select " + params[0];
			for (int i=1;i<params.length;i++){
				finalQuery = finalQuery + ", " + params[i]; 
			}
		}
		finalQuery = finalQuery + " from ";
		if(tables == null || tables.length == 0){
			AppLogger.logError(DatabaseConnector.class, "SQLException", "Malformed Query: no tables specified");
			return null;
		} else {
			finalQuery = finalQuery + tables[0];
			for (int i=1;i<tables.length;i++){
				finalQuery = finalQuery + " natural join " + tables[i]; 
			}
		}
		if(filter != null && filter.length() > 0){
			finalQuery = finalQuery + " where " + filter;
		} 
		if(orders != null && orders.length > 0){
			finalQuery = finalQuery + " order by " + orders[0];
			for (int i=1;i<orders.length;i++){
				finalQuery = finalQuery + ", " + orders[i]; 
			}
		}
		return finalQuery;
	}
	
	public static void createDatabase(String url, String dbName, String user, String psw){
		ScriptRunner sr;
		Reader reader = null;
		try {
			sr = new ScriptRunner(DriverManager.getConnection(url, user, psw), false, false);
			reader = new BufferedReader(new FileReader(new File("sql/CreateExpDatabase.sql")));
			sr.runScript(reader);
		} catch(Exception ex){
			AppLogger.logException(DatabaseConnector.class, ex, "Unable to create database");
		} 
	}

}
