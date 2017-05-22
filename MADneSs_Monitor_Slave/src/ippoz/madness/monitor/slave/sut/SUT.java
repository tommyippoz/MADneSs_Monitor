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

// TODO: Auto-generated Javadoc
/**
 * The Class SUT.
 *
 * @author Tommy
 */
public abstract class SUT {
	
	/** The Constant PARAMETER_TAGS. */
	private static final String[] PARAMETER_TAGS = new String[]{"SCRIPT_PATH", "START_SCRIPT_NAME", "SHUTDOWN_SCRIPT_NAME", "START_DELAY", "SHUTDOWN_DELAY", "SUT_FOLDER"};
	
	/** The SUT name. */
	private String name;
	
	/** The script folder. */
	protected String scriptFolder;
	
	/** The start script name. */
	protected String startScriptName;
	
	/** The shutdown script name. */
	protected String shutdownScriptName;
	
	/** The injection list. */
	protected LinkedList<Injection> injList;
	
	/** The start delay. */
	private long startDelay;
	
	/** The shutdown delay. */
	private long shutdownDelay;
	
	/** The folder of the SUT. */
	protected String sutFolder;
	
	/**
	 * Instantiates a new SUT.
	 *
	 * @param name the name
	 * @param prefFile the preferences file
	 */
	protected SUT(String name, File prefFile){
		this.name = name;
		readParametersFromFile(prefFile);
	}
	
	/**
	 * Instantiates a new SUT.
	 *
	 * @param name the name
	 * @param scriptFolder the script folder
	 * @param startScriptName the start script name
	 * @param shutdownScriptName the shutdown script name
	 */
	public SUT(String name, String scriptFolder, String startScriptName, String shutdownScriptName) {
		this.name = name;
		this.scriptFolder = scriptFolder;
		this.startScriptName = startScriptName;
		this.shutdownScriptName = shutdownScriptName;
	}
	
	/**
	 * Gets the start script name.
	 *
	 * @return the start script name
	 */
	public String getStartScriptName() {
		return startScriptName;
	}

	/**
	 * Gets the shutdown script name.
	 *
	 * @return the shutdown script name
	 */
	public String getShutdownScriptName() {
		return shutdownScriptName;
	}

	/**
	 * Sets the injections.
	 *
	 * @param injList the new injections
	 */
	public void setInjections(LinkedList<Injection> injList){
		this.injList = injList;
	}

	/**
	 * Gets the SUT name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Start SUT.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public void startSUT() throws IOException, InterruptedException {
		AppLogger.logInfo(getClass(), "Calling start routine ...");
		AppUtility.runScript(scriptFolder + "/" + startScriptName, null, true, true, false);
		Thread.sleep(getStartDelay());
		activateInjections();
		AppLogger.logInfo(getClass(), "SUT started");
	}

	/**
	 * Activate injections.
	 */
	private void activateInjections() {
		if(injList != null){
			for(Injection inj : injList){
				inj.setupInjection();
			}
		}
	}
	
	/**
	 * Flush injections.
	 */
	private void flushInjections() {
		if(injList != null) {
			for(Injection inj : injList){
				inj.flushInjection();
			}
		}
	}
	
	/**
	 * Gets the injection timestamps.
	 *
	 * @return the injection timestamps
	 */
	public String[] getInjectionTimestamps(){
		int i = 0;
		String[] outList = new String[injList.size()];
		for(Injection inj : injList){
			outList[i++] = inj.getInjTag() + ";" + inj.getInjDetail() + ";" + inj.getInjTimestamp();
		}
		return outList;
	}

	/**
	 * Shutdown SUT.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public void shutdownSUT() throws IOException, InterruptedException {
		AppLogger.logInfo(getClass(), "Calling shutdown routine ...");
		AppUtility.runScript(scriptFolder + "/" + shutdownScriptName, null, true, true, false);
		Thread.sleep(getShutdownDelay());
		flushInjections();
		AppLogger.logInfo(getClass(), "SUT shutdowned");
	}
	
	/**
	 * Read parameters from file.
	 *
	 * @param prefFile the preferences file
	 */
	protected void readParametersFromFile(File prefFile) {
		HashMap<String, String> readedParameters;
		try {
			readedParameters = AppUtility.loadPreferences(prefFile, PARAMETER_TAGS);
			scriptFolder = readedParameters.get("SCRIPT_PATH");
			startScriptName = readedParameters.get("START_SCRIPT_NAME");
			shutdownScriptName = readedParameters.get("SHUTDOWN_SCRIPT_NAME");
			startDelay = Long.parseLong(readedParameters.get("START_DELAY"));
			shutdownDelay = Long.parseLong(readedParameters.get("SHUTDOWN_DELAY"));
			sutFolder = readedParameters.get("SUT_FOLDER");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to process SUT parameters");
		}
	}

	/**
	 * Checks for injections.
	 *
	 * @return true, if successful
	 */
	public boolean hasInjections() {
		return injList != null;
	}
	
	/**
	 * Gets the start delay.
	 *
	 * @return the start delay
	 */
	protected long getStartDelay() {
		return startDelay;
	}

	/**
	 * Gets the shutdown delay.
	 *
	 * @return the shutdown delay
	 */
	protected long getShutdownDelay() {
		return shutdownDelay;
	}

	/**
	 * Checks for injection.
	 *
	 * @param toCompare the to compare
	 * @return true, if successful
	 */
	public abstract boolean hasInjection(String toCompare);

	/**
	 * Gets the start script path.
	 *
	 * @return the start script path
	 */
	public String getStartScriptPath() {
		return scriptFolder + "/" + startScriptName;
	}
	
}
