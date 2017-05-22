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
 * The Class ScriptProbe.
 *
 * @author Tommy
 */
public abstract class ScriptProbe extends Probe {

	/** The file path. */
	private String filePath;
	
	/** The file arguments. */
	private String fileArgs;
	
	/** The probe process. */
	private Process probeProcess;
	
	/**
	 * Instantiates a new script probe.
	 *
	 * @param filePath the file path
	 * @param fileArgs the file arguments
	 * @param probeLayer the probe layer
	 * @param probeName the probe name
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 */
	public ScriptProbe(String filePath, String fileArgs, LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		super(probeLayer, probeName, receiverIp, probePort);
		this.fileArgs = fileArgs;
		this.filePath = filePath;
		if(!(new File(filePath)).exists())
			AppLogger.logError(getClass(), "NoSuchProbe", "Unable to find probe script in " + filePath);
	}

	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Gets the file arguments.
	 *
	 * @return the file arguments
	 */
	public String getFileArgs() {
		return fileArgs;
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#startProbe()
	 */
	@Override
	public void startProbe() {
		try {
			probeProcess = AppUtility.runScript(filePath, fileArgs, true, false, false);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to start probe " + getProbeName());
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#shutdownProbe()
	 */
	@Override
	public void shutdownProbe() {
		probeProcess.destroy();
	}

}
