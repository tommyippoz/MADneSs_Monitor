/**
 * 
 */
package ippoz.multilayer.monitor.master.probes.receiver;

import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class ProbeReceiverManager {
	
	private LinkedList<ProbeReceiver> receivers;
	
	public ProbeReceiverManager(){
		receivers = new LinkedList<ProbeReceiver>();
	}
	
	public void addReceiver(ProbeReceiver newReceiver){
		receivers.add(newReceiver);
	}
	
	public boolean testReceiver(){
		for(ProbeReceiver pReceiver : receivers){
			if(!pReceiver.testConnection())
				return false;
		}
		return true;
	}
	
	public void startReceivers(){
		for(ProbeReceiver pReceiver : receivers){
			new Thread(pReceiver).start();
			//AppLogger.logInfo(getClass(), "Receiver " + pReceiver.getReceiverName() + " started");
		}
	}

	public void closeReceivers() {
		for(ProbeReceiver pReceiver : receivers){
			pReceiver.endReading();
			//AppLogger.logInfo(getClass(), "Receiver " + pReceiver.getReceiverName() + " shutdowned");
		}
	}

}
