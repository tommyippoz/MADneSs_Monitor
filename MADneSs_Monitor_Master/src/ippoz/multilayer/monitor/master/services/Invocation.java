/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

import ippoz.multilayer.monitor.master.database.DatabaseManager;

/**
 * @author Tommy
 *
 */
public class Invocation {
	
	private Service service;
	private String methodName;
	private long startTime;
	private long endTime;
	private String responseCode;
	
	public Invocation(Service service, String methodName, long startTime, long endTime, String responseCode) {
		this.service = service;
		this.methodName = methodName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.responseCode = responseCode;
	}

	public Invocation(Service service, String methodName, String startTime, String endTime, String responseCode) {
		this(service, methodName, Long.parseLong(startTime), Long.parseLong(endTime), responseCode);
	}

	public Service getService() {
		return service;
	}
	
	public String getMethod(){
		return methodName;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
	
	public String getResponseCode(){
		return responseCode;
	}
	
	public void insertIntoDatabase(DatabaseManager dbManager){
		// TODO
	}

	

}
