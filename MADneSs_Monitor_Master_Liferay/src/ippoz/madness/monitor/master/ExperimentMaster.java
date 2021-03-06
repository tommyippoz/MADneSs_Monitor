/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.PreferencesManager;

import java.io.File;
import java.io.IOException;
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
		LiferayMasterManager master;
		try {
			masterPreferences = new PreferencesManager(new File("masterPreferences.preferences"));
			master = new LiferayMasterManager(masterPreferences);
			master.setupEnvironment();
			master.startExperimentalCampaign();
			master.flush();
			System.exit(0);
		} catch (IOException e) {
			AppLogger.logException(ExperimentMaster.class, e, "Unhandled IOException");
		} catch (SQLException e) {
			AppLogger.logException(ExperimentMaster.class, e, "Unhandled SQLException");
		}
	}

}
