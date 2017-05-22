/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;

/**
 * The Class Probe. Represents the basic probe, which communicates data through the Internet.
 *
 * @author Tommy
 */
public abstract class Probe implements Runnable {
	
	/** The probe layer. */
	private LayerType probeLayer;
	
	/** The probe name. */
	private String probeName;
	
	/** The receiver IP address. */
	private String receiverIp;
	
	/** The probe port. */
	private int probePort;

	/**
	 * Instantiates a new probe.
	 *
	 * @param probeLayer the probe layer
	 * @param probeName the probe name
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 */
	public Probe(LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		this.probeLayer = probeLayer;
		this.probeName = probeName;
		this.receiverIp = receiverIp;
		this.probePort = probePort;
	}

	/**
	 * Gets the probe layer.
	 *
	 * @return the probe layer
	 */
	public LayerType getProbeLayer() {
		return probeLayer;
	}

	/**
	 * Gets the probe name.
	 *
	 * @return the probe name
	 */
	public String getProbeName() {
		return probeName;
	}
	
	/**
	 * Gets the receiver IP address.
	 *
	 * @return the receiver IP address
	 */
	protected String getReceiverIp(){
		return receiverIp;
	}
	
	/**
	 * Gets the probe port.
	 *
	 * @return the probe port
	 */
	protected int getProbePort(){
		return probePort;
	}
	
	/**
	 * Setup parameters.
	 */
	public abstract void setupParameters();
	
	/**
	 * Start probe.
	 */
	public abstract void startProbe();
	
	/**
	 * Shutdown probe.
	 */
	public abstract void shutdownProbe();

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if(canRun()){
			startProbe();
		}
	}
	
	/**
	 * Checks if the probe can run.
	 *
	 * @return true, if the probe can run in the host system
	 */
	public abstract boolean canRun();
	
}
