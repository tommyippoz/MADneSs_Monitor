/**
 * 
 */
package ippoz.madness.monitor.slave.sut;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;
import ippoz.madness.monitor.slave.sut.injection.Injection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public abstract class SUT {
	
	private static final String[] PARAMETER_TAGS = new String[]{"SCRIPT_PATH", "START_SCRIPT_NAME", "SHUTDOWN_SCRIPT_NAME", "START_DELAY", "SHUTDOWN_DELAY"};
	
	private String name;
	protected String scriptFolder;
	protected String startScriptName;
	protected String shutdownScriptName;
	protected LinkedList<Injection> injList;
	private long startDelay;
	private long shutdownDelay;
	
	protected SUT(String name, File prefFile){
		this.name = name;
		readParametersFromFile(prefFile);
	}
	
	public SUT(String name, String scriptFolder, String startScriptName, String shutdownScriptName) {
		this.name = name;
		this.scriptFolder = scriptFolder;
		this.startScriptName = startScriptName;
		this.shutdownScriptName = shutdownScriptName;
	}
	
	public void setInjections(LinkedList<Injection> injList){
		this.injList = injList;
	}

	public String getName() {
		return name;
	}

	public void startSUT() throws IOException, InterruptedException {
		AppLogger.logInfo(getClass(), "Calling start routine ...");
		AppUtility.runScript(scriptFolder + "/" + startScriptName, null, true, true, false);
		Thread.sleep(getStartDelay());
		activateInjections();
		AppLogger.logInfo(getClass(), "SUT started");
	}

	private void activateInjections() {
		if(injList != null){
			for(Injection inj : injList){
				inj.setupInjection();
			}
		}
	}
	
	private void flushInjections() {
		if(injList != null) {
			for(Injection inj : injList){
				inj.flushInjection();
			}
		}
	}
	
	public String[] getInjectionTimestamps(){
		int i = 0;
		String[] outList = new String[injList.size()];
		for(Injection inj : injList){
			outList[i++] = inj.getInjType() + ";" + inj.getInjTag() + ";" + inj.getInjTimestamp();
		}
		return outList;
	}

	public void shutdownSUT() throws IOException, InterruptedException {
		AppLogger.logInfo(getClass(), "Calling shutdown routine ...");
		AppUtility.runScript(scriptFolder + "/" + shutdownScriptName, null, true, true, true);
		Thread.sleep(getShutdownDelay());
		flushInjections();
		AppLogger.logInfo(getClass(), "SUT shutdowned");
	}
	
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

	public boolean hasInjections() {
		return injList != null;
	}
	
	protected long getStartDelay() {
		return startDelay;
	}

	protected long getShutdownDelay() {
		return shutdownDelay;
	}
	
}
