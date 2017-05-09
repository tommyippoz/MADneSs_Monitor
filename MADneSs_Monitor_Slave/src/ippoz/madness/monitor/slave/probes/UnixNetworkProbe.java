/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author root
 *
 */
public class UnixNetworkProbe extends IteratingCommandProbe {

	private HashMap<String, String> paramNames;
	
	public UnixNetworkProbe(String receiverIp, int probePort) {
		super("dstat", "-n --tcp", LayerType.UNIX_NETWORK, "UnixNetwork", receiverIp, probePort);
	}
	
	@Override
	public void setupParameters() {
		setupParamNames();	
	}

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

	@Override
	protected boolean isHeader(String line) {
		return line.trim().endsWith("--");
	}

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
