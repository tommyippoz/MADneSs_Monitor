/**
 * 
 */
package ippoz.multilayer.monitor.master.performance;

import java.util.HashMap;
import java.util.LinkedList;

import ippoz.multilayer.monitor.master.probes.receiver.ProbeReceiver;

/**
 * @author tommaso
 *
 */
public class ExperimentTiming {
	
	private static HashMap<ProbeReceiver, LinkedList<ObservationTiming>> probeObservations;
	
	public static void setProbeObservation(ProbeReceiver receiver, int indNumber, int ot, int pmtt, int dat) {
		if(probeObservations == null){
			probeObservations = new HashMap<ProbeReceiver, LinkedList<ObservationTiming>>();
		}
		if(probeObservations.get(receiver) == null){
			probeObservations.put(receiver, new LinkedList<ObservationTiming>());
		}
		probeObservations.get(receiver).add(new ObservationTiming(indNumber, ot, pmtt, dat));
	}
	
	public static HashMap<ProbeReceiver, LinkedList<ObservationTiming>> getTimings(){
		return probeObservations;
	}

	public static void resetTimings() {
		probeObservations = null;
	}

}
