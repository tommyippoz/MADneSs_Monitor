/**
 * 
 */
package ippoz.madness.monitor.slave;

import ippoz.madness.commons.support.AppLogger;

import java.io.File;
import java.net.InetAddress;

/**
 * @author Tommy
 *
 */
public class ExperimentSlave {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SlaveManager sManager = new SlaveManager(new File("slavePreferences.preferences"));
			AppLogger.logInfo(ExperimentSlave.class, "MADneSs will start on Host '" + InetAddress.getLocalHost().getHostName() + "'");
			sManager.startListener();
		} catch (Exception ex) {
			AppLogger.logException(ExperimentSlave.class, ex, "");
		}
		
	}

}
