/**
 * 
 */
package ippoz.madness.monitor.slave.sut;

import ippoz.madness.commons.support.AppUtility;

import java.io.File;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class Liferay612SUT. Used for testing Secure!
 *
 * @author Tommy
 */
public class Liferay612SUT extends SUT {

	/**
	 * Instantiates a new Liferay 6.1.2 SUT.
	 *
	 * @param name the SUT name
	 * @param prefFile the preferences file
	 */
	public Liferay612SUT(String name, File prefFile) {
		super(name, prefFile);
	}	

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.SUT#startSUT()
	 */
	@Override
	public void startSUT() throws IOException, InterruptedException {
		super.startSUT();
		AppUtility.runScript("pkill -f firefox", null, false, true, true);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.SUT#shutdownSUT()
	 */
	@Override
	public void shutdownSUT() throws IOException, InterruptedException {
		super.shutdownSUT();
		AppUtility.runScript("pkill -f firefox", null, false, true, true);
		AppUtility.runScript("rm -f output_agent", null, false, true, true);
		AppUtility.runScript("rm -f secure_output_agent", null, false, true, true);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.SUT#hasInjection(java.lang.String)
	 */
	@Override
	public boolean hasInjection(String toCompare) {
		return toCompare.equals("CPU") || toCompare.equals("DISK") || toCompare.equals("NETWORK") ||
				toCompare.equals("MEMORY") || toCompare.equals("DEADLOCK") || toCompare.equals("NET_PERM");
	}


}
