/**
 * 
 */
package ippoz.multilayer.monitor.master.workload;

import ippoz.multilayer.monitor.master.services.Invocation;
import ippoz.multilayer.monitor.master.services.Service;

import java.util.LinkedList;

/**
 * The Interface Workload.
 * Defines a basic type of workload.
 *
 * @author Tommy
 */
public abstract class Workload {
	
	private String name;
	
	/** The minimum execution time of the workload. */
	private int minTime;
	
	/** The maximum execution time of the workload. */
	private int maxTime;
	
	/**
	 * Instantiates a new workload.
	 *
	 * @param minTime the minimum execution time of the workload
	 * @param maxTime the maximum execution time of the workload
	 */
	protected Workload(String name, int minTime, int maxTime){
		this.name = name;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}

	/**
	 * Gets the minimum execution time of the workload.
	 *
	 * @return the minimum execution time of the workload
	 */
	public int getMinExecutionTime(){
		return minTime;
	}
	
	/**
	 * Gets the maximum execution time of the workload.
	 *
	 * @return the maximum execution time of the workload
	 */
	public int getMaxExecutionTime(){
		return maxTime;
	}
	
	/**
	 * Gets the name of the workload.
	 *
	 * @return the name of the workload
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Checks if the workload is valid.
	 *
	 * @return true, if is valid
	 */
	public abstract boolean isValid();
	
	/**
	 * Services used by the workload.
	 *
	 * @return the list of services
	 */
	public abstract LinkedList<Service> usedServices();
	
	/**
	 * Checks if the workload is running.
	 *
	 * @return true, if is running
	 */
	public abstract boolean isRunning();
	
	/**
	 * Runs the workload.
	 *
	 * @param seeOutput flag that indicates if the output goes to the console
	 */
	public abstract void runWorkload(boolean seeOutput);
	
	/**
	 * Executed invocations.
	 *
	 * @return the list of invocations
	 */
	public abstract LinkedList<Invocation> executedInvocations();
	
	/**
	 * Clones workload.
	 *
	 * @return the cloned workload
	 */
	public abstract Workload cloneWorkload();

	/**
	 * Flushes.
	 */
	public abstract void flush();
	
	/**
	 * Checks if the service exists in the list.
	 *
	 * @param service the service
	 * @param serviceList the service list
	 * @return true, if the service exists in the list
	 */
	public static boolean existsIn(Service service, LinkedList<Service> serviceList){
		for(Service currentService : serviceList){
			if(service.compareTo(currentService) == 0)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if the service exists in the list.
	 *
	 * @param service the service
	 * @param serviceList the service list
	 * @return true, if the service exists in the list
	 */
	public static boolean existsIn(String serviceName, LinkedList<Service> serviceList){
		for(Service currentService : serviceList){
			if(serviceName.equals(currentService.getName()))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a service if existing in the list.
	 *
	 * @param service the service
	 * @param serviceList the service list
	 * @return true, if the service exists in the list
	 */
	public static Service getServiceFrom(String serviceName, LinkedList<Service> serviceList){
		for(Service currentService : serviceList){
			if(serviceName.equals(currentService.getName()))
				return currentService;
		}
		return null;
	}
	
}
