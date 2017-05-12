/**
 * 
 */
package ippoz.madness.monitor.slave.sut;

import ippoz.madness.commons.support.AppUtility;

import java.io.File;
import java.io.IOException;

/**
 * @author Tommy
 *
 */
public class Liferay612SUT extends SUT {

	public Liferay612SUT(String name, File prefFile) {
		super(name, prefFile);
	}	

	@Override
	public void startSUT() throws IOException, InterruptedException {
		super.startSUT();
		AppUtility.runScript("pkill -f firefox", null, false, true, true);
	}

	@Override
	public void shutdownSUT() throws IOException, InterruptedException {
		super.shutdownSUT();
		AppUtility.runScript("pkill -f firefox", null, false, true, true);
		AppUtility.runScript("rm -f output_agent", null, false, true, true);
		AppUtility.runScript("rm -f secure_output_agent", null, false, true, true);
	}


}
