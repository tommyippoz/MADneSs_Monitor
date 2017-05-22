/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import java.io.File;
import java.util.HashMap;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.monitor.slave.probes.mbean.BeanManager;

// TODO: Auto-generated Javadoc
/**
 * The Class JMXLocalProbe. Reads data directly from the JVM.
 *
 * @author ippoz
 */
public class JMXLocalProbe extends CycleProbe {
	
	/** The b manager. */
	private BeanManager bManager;

	/**
	 * Instantiates a new JMX local probe.
	 *
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 * @param beanPrefFile the bean preferences file
	 */
	public JMXLocalProbe(String receiverIp, int probePort, File beanPrefFile) {
		super(LayerType.JVM, "JMX", receiverIp, probePort);
		try {
			bManager = new BeanManager();
			bManager.writeBeansToFile("AvailableBeans.csv");
			if(beanPrefFile.exists()){
				bManager.loadBeanPreferences(beanPrefFile);
			} else AppLogger.logError(getClass(), "FileNotFoundException", "Unable to find file for MBean preferences");
		} catch (Exception ex){
			AppLogger.logException(getClass(), ex, "Error while Initializing MBeans");
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.CycleProbe#readParams()
	 */
	@Override
	protected HashMap<String, String> readParams() {
		return bManager.getObsValues();
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#setupParameters()
	 */
	@Override
	public void setupParameters() {
		// TODO
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#canRun()
	 */
	@Override
	public boolean canRun() {
		return true;
	}

}
