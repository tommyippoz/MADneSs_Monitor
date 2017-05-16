/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.PreferencesManager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;


/**
 * The Class ExperimentMaster.
 * The main class of the master of the experiments. This is hosted on a machine that is not the observed one.
 *
 * @author Tommy
 */
public class ExperimentMaster {
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		PreferencesManager masterPreferences;
		JSeduiteMasterManager master;
		try {
			masterPreferences = new PreferencesManager(new File("masterPreferences.preferences"));
			AppLogger.logInfo(ExperimentMaster.class, "MADneSs Server will start on host '" + InetAddress.getLocalHost().getHostName() + "'");
			master = new JSeduiteMasterManager(masterPreferences);
			if(master.isInitialized()){
				if(master.setupEnvironment()){
					master.startExperimentalCampaign();
				} else AppLogger.logInfo(ExperimentMaster.class, "Unable to read workloads. MADneSs will stop");
				master.flush();
			}
		} catch (IOException e) {
			AppLogger.logException(ExperimentMaster.class, e, "Unhandled IOException");
		} catch (SQLException e) {
			AppLogger.logException(ExperimentMaster.class, e, "Unhandled SQLException");
		}
	}

}
