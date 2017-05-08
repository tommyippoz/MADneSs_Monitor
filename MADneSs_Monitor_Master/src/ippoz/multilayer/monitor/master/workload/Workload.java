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
	protected Workload(int minTime, int maxTime){
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
	public abstract String getName();
	
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
	
}
