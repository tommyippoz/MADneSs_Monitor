/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.support.AppLogger;

import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class ProbeManager {

	private LinkedList<Probe> probes;
	
	public ProbeManager(){
		probes = new LinkedList<Probe>();
	}
	
	public void addProbe(Probe newProbe){
		newProbe.setupParameters();
		probes.add(newProbe);
		AppLogger.logInfo(getClass(), "Added probe " + newProbe.getProbeName());
	}
	
	public void startProbes(){
		for(Probe probe : probes){
			new Thread(probe).start();
			AppLogger.logInfo(getClass(), "Probe " + probe.getProbeName() + " started");
		}
	}
	
	public void shutdownProbes(){
		for(Probe probe : probes){
			probe.shutdownProbe();
			AppLogger.logInfo(getClass(), "Probe " + probe.getProbeName() + " shutdowned");
		}
		probes.clear();
	}
	
}
