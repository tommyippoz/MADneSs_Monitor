/**
 * 
 */
package ippoz.multilayer.monitor.slave.sut;

import ippoz.multilayer.monitor.support.AppLogger;
import ippoz.multilayer.monitor.support.AppUtility;

import java.io.IOException;


/**
 * @author root
 *
 */
public class BashInjection extends Injection {

	private String startScript;
	private String shutdownScript;
	private Process process;
	
	public BashInjection(String injType, String injTag, long delay, String startScript, String shutdownScript) {
		super(injType, injTag, delay);
		this.startScript = startScript;
		this.shutdownScript = shutdownScript;
	}

	@Override
	public void inject() {
		try {
			process = AppUtility.runScript(startScript, null, false, false, false);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to Inject bash script");
		}
	}

	@Override
	public void flush() {
		try {
			//AppUtility.runScript(shutdownScript, null, false, false, false);
			process.destroy();
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to Inject bash script");
		}
	}

}
