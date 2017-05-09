/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;

import java.io.File;
import java.io.IOException;

/**
 * @author Tommy
 *
 */
public abstract class ScriptProbe extends Probe {

	private String filePath;
	private String fileArgs;
	private Process probeProcess;
	
	public ScriptProbe(String filePath, String fileArgs, LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		super(probeLayer, probeName, receiverIp, probePort);
		this.fileArgs = fileArgs;
		this.filePath = filePath;
		if(!(new File(filePath)).exists())
			AppLogger.logError(getClass(), "NoSuchProbe", "Unable to find probe script in " + filePath);
	}

	public String getFilePath() {
		return filePath;
	}

	public String getFileArgs() {
		return fileArgs;
	}

	@Override
	public void startProbe() {
		try {
			probeProcess = AppUtility.runScript(filePath, fileArgs, true, false, false);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to start probe " + getProbeName());
		}
	}

	@Override
	public void shutdownProbe() {
		probeProcess.destroy();
	}

}
