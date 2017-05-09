/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.multilayer.commons.layers.LayerType;

/**
 * @author Tommy
 *
 */
public abstract class Probe implements Runnable {
	
	private LayerType probeLayer;
	private String probeName;
	private String receiverIp;
	private int probePort;

	public Probe(LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		this.probeLayer = probeLayer;
		this.probeName = probeName;
		this.receiverIp = receiverIp;
		this.probePort = probePort;
	}

	public LayerType getProbeLayer() {
		return probeLayer;
	}

	public String getProbeName() {
		return probeName;
	}
	
	protected String getReceiverIp(){
		return receiverIp;
	}
	
	protected int getProbePort(){
		return probePort;
	}
	
	public abstract void setupParameters();
	
	public abstract void startProbe();
	
	public abstract void shutdownProbe();

	@Override
	public void run() {
		startProbe();
	}
	
}
