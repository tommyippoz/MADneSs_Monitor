/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.commons.support.AppLogger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author root
 *
 */
public abstract class CycleProbe extends Probe {
	
	private static final int OBSERVATION_MS_DELAY = 1000;
	private static final int CONN_ATTEMPTS_NUMBER = 20;
	
	private boolean halt;

	public CycleProbe(LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		super(probeLayer, probeName, receiverIp, probePort);
		halt = false;
	}

	@Override
	public void startProbe() {
		long startTime;
		int attCount = 1;
		Socket socket = null;
		ObjectOutputStream outStream = null;	 
		try {
			while(socket == null && attCount <= CONN_ATTEMPTS_NUMBER) {
				AppLogger.logInfo(getClass(), "Attempt " + attCount + ": Waiting for the Socket at " + getReceiverIp() + ":" + getProbePort());
				try {
					socket = new Socket(getReceiverIp(), getProbePort());
					AppLogger.logInfo(getClass(), "Server found at attempt " + attCount);
				} catch (IOException e) {
					e.printStackTrace();
					Thread.sleep(1000);
				}
				attCount++;
			}
			if(socket != null) {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				startTime = System.currentTimeMillis();
				while(socket.isConnected() && !halt){
				    outStream.writeObject(buildJSON(startTime, readParams()).toString(2));
				    startTime = startTime + OBSERVATION_MS_DELAY;
				    Thread.sleep(startTime - System.currentTimeMillis());
				}
			} else AppLogger.logError(getClass(), "SocketError", "Server not found");
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Error");
		} finally {
			try {
				if(socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private JSONObject buildJSON(long startTime, HashMap<String, String> attributes) {
		JSONObject partial, jObj = new JSONObject();
		JSONArray jArr = new JSONArray();
		jObj.put("time_ms", startTime);
		jObj.put("datetime", new Date().toString());
		for(String attName : attributes.keySet()){
			partial = new JSONObject();
			partial.accumulate("attributeName", attName);
			partial.accumulate("attributeValue", attributes.get(attName));
			jArr.add(partial);
		}
		jObj.put("processingTime_ms", (System.currentTimeMillis() - startTime));
		jObj.put("observations", jArr);
		jObj.put("delivery_time_ms", System.currentTimeMillis());
		return jObj;
	}

	protected abstract HashMap<String, String> readParams();

	@Override
	public void shutdownProbe() {
		halt = true;
	}

}
