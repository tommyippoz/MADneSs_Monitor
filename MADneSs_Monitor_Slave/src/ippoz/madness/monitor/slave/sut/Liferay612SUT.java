/**
 * 
 */
package ippoz.madness.monitor.slave.sut;


import ippoz.multilayer.commons.support.AppLogger;
import ippoz.multilayer.commons.support.AppUtility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Tommy
 *
 */
public class Liferay612SUT extends SUT {
	
	private static final String[] PARAMETER_TAGS = new String[]{"SCRIPT_PATH", "START_SCRIPT_NAME", "SHUTDOWN_SCRIPT_NAME", "START_DELAY", "SHUTDOWN_DELAY"};
	
	private long startDelay;
	private long shutdownDelay;

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

	@Override
	protected long getStartDelay() {
		return startDelay;
	}

	@Override
	protected long getShutdownDelay() {
		return shutdownDelay;
	}

	@Override
	protected void readParametersFromFile(File prefFile) {
		HashMap<String, String> readedParameters;
		try {
			readedParameters = AppUtility.loadPreferences(prefFile, PARAMETER_TAGS);
			scriptFolder = readedParameters.get("SCRIPT_PATH");
			startScriptName = readedParameters.get("START_SCRIPT_NAME");
			shutdownScriptName = readedParameters.get("SHUTDOWN_SCRIPT_NAME");
			startDelay = Long.parseLong(readedParameters.get("START_DELAY"));
			shutdownDelay = Long.parseLong(readedParameters.get("SHUTDOWN_DELAY"));
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to process SUT parameters");
		}
	}

}
