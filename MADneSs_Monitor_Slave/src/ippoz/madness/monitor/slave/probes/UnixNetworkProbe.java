/**
 * 
 */
package ippoz.madness.monitor.slave.probes;
import ippoz.madness.commons.layers.LayerType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The Class UnixNetworkProbe. Used for monitoring network data in Linux systems through "dstat"
 *
 * @author ippoz
 */
public class UnixNetworkProbe extends IteratingCommandProbe {

	/** The parameters names. */
	private HashMap<String, String> paramNames;
	
	/**
	 * Instantiates a new UNIX network probe.
	 *
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 */
	public UnixNetworkProbe(String receiverIp, int probePort) {
		super("dstat", "-n --tcp", LayerType.UNIX_NETWORK, "UnixNetwork", receiverIp, probePort);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#setupParameters()
	 */
	@Override
	public void setupParameters() {
		setupParamNames();	
	}

	/**
	 * Setup parameters names.
	 */
	private void setupParamNames() {
		paramNames = new HashMap<String, String>();
		paramNames.put("recv", "Net_Received");
		paramNames.put("send", "Net_Sent");
		paramNames.put("lis", "Tcp_Listen");
		paramNames.put("act", "Tcp_Established");
		paramNames.put("syn", "Tcp_Syn");
		paramNames.put("tim", "Tcp_TimeWait");
		paramNames.put("clo", "Tcp_Close");
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.IteratingCommandProbe#isHeader(java.lang.String)
	 */
	@Override
	protected boolean isHeader(String line) {
		return line.trim().endsWith("--");
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.CycleProbe#readParams()
	 */
	@Override
	protected HashMap<String, String> readParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		Iterator<String> keyIt = paramNames.keySet().iterator();
		synchronized(lastReaded){
			StringTokenizer lt = new StringTokenizer(lastReaded.replace("|", ""));
			while(lt.hasMoreTokens()){
				params.put(paramNames.get(keyIt.next()), parseQuantity(lt.nextToken()));
			}
		}
		return params;
	}

	/**
	 * Parses the quantity that is read, by transforming all the quantities in "bytes".
	 *
	 * @param splitted the starting quantity
	 * @return the parsed and standardized quantity
	 */
	private String parseQuantity(String splitted) {
		String cleared = splitted.trim().toUpperCase();
		if(cleared.endsWith("B"))
			return splitted.substring(0, splitted.length()-1);
		else if(cleared.endsWith("K"))
			return String.valueOf(1000*Integer.parseInt(splitted.substring(0, splitted.length()-1)));
		else if(cleared.endsWith("M"))
			return String.valueOf(1000000*Integer.parseInt(splitted.substring(0, splitted.length()-1)));
		else return splitted;
	}

}
