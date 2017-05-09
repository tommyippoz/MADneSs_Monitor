/**
 * 
 */
package ippoz.madness.monitor.slave.sut;

import ippoz.multilayer.commons.support.AppLogger;
import ippoz.multilayer.commons.support.AppUtility;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public abstract class SUT {

	private String name;
	protected String scriptFolder;
	protected String startScriptName;
	protected String shutdownScriptName;
	protected LinkedList<Injection> injList;
	
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
	
	protected abstract void readParametersFromFile(File prefFile);
	
	protected abstract long getStartDelay();
	
	protected abstract long getShutdownDelay();

	public boolean hasInjections() {
		return injList != null;
	}
	
}
