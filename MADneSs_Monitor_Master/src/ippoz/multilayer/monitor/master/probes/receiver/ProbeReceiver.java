/**
 * 
 */
package ippoz.multilayer.monitor.master.probes.receiver;

import java.io.InputStream;

import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.monitor.master.observation.Observation;
import ippoz.multilayer.monitor.master.observation.ObservationCollector;

/**
 * @author Tommy
 *
 */
public abstract class ProbeReceiver implements Runnable {
	
	protected String receiverName;
	protected ObservationCollector collector;
	private LayerType type;
	
	public ProbeReceiver(String receiverName, ObservationCollector collector, LayerType type){
		this.receiverName = receiverName;
		this.collector = collector;
		this.type = type;
	}
	
	public abstract boolean canRead();
	
	public abstract void startReading();
	
	public abstract void endReading();
	
	public abstract boolean testConnection();
	
	public abstract Observation parseObservation(InputStream inStream);
	
	public LayerType getLayerType(){
		return type;
	}
	
	public String getReceiverName(){
		return receiverName;
	}

	@Override
	public void run() {
		startReading();
	}
	
	
}
