/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.support.AppLogger;

import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class ProbeManager. Manages all the instantiated probes.
 *
 * @author Tommy
 */
public class ProbeManager {

	/** The list of probes. */
	private LinkedList<Probe> probes;
	
	/**
	 * Instantiates a new probe manager.
	 */
	public ProbeManager(){
		probes = new LinkedList<Probe>();
	}
	
	/**
	 * Adds a probe to the manager.
	 *
	 * @param newProbe the new probe
	 */
	public void addProbe(Probe newProbe){
		newProbe.setupParameters();
		probes.add(newProbe);
		AppLogger.logInfo(getClass(), "Added probe " + newProbe.getProbeName());
	}
	
	/**
	 * Starts all the probes in the list.
	 */
	public void startProbes(){
		for(Probe probe : probes){
			new Thread(probe).start();
			AppLogger.logInfo(getClass(), "Probe " + probe.getProbeName() + " started");
		}
	}
	
	/**
	 * Shutdowns all the probes in the list.
	 */
	public void shutdownProbes(){
		for(Probe probe : probes){
			probe.shutdownProbe();
			AppLogger.logInfo(getClass(), "Probe " + probe.getProbeName() + " shutdowned");
		}
		probes.clear();
	}
	
}
