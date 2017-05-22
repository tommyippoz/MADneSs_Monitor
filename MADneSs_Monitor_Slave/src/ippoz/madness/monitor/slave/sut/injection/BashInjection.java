/**
 * 
 */
package ippoz.madness.monitor.slave.sut.injection;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;

import java.io.IOException;

/**
 * The Class BashInjection.
 *
 * @author ippoz
 */
public class BashInjection extends Injection { 

	/** The start script. */
	private String startScript;
	
	/** The shutdown script. */
	private String shutdownScript;
	
	/** The process. */
	private Process process;
	
	/**
	 * Instantiates a new bash injection.
	 *
	 * @param injTag the injection tag
	 * @param delay the injection delay
	 * @param startScript the start script
	 * @param shutdownScript the shutdown script
	 */
	public BashInjection(String injTag, long delay, String startScript, String shutdownScript) {
		super(injTag, delay);
		this.startScript = startScript;
		this.shutdownScript = shutdownScript;
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.injection.Injection#inject()
	 */
	@Override
	public void inject() {
		try {
			process = AppUtility.runScript(startScript, null, false, false, false);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to Inject bash script");
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.injection.Injection#flush()
	 */
	@Override
	public void flush() {
		try {
			AppUtility.runScript(shutdownScript, null, false, false, false);
			process.destroy();
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to Inject bash script");
		}
	}

}
