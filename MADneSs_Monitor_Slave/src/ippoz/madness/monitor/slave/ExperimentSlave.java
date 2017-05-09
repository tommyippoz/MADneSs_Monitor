/**
 * 
 */
package ippoz.madness.monitor.slave;

import java.io.File;

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
