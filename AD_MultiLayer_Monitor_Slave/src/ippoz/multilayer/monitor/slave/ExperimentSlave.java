/**
 * 
 */
package ippoz.multilayer.monitor.slave;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import ippoz.multilayer.monitor.slave.SlaveManager;
import ippoz.multilayer.monitor.support.AppUtility;

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
			new SlaveManager(new File("slavePreferences.preferences"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
