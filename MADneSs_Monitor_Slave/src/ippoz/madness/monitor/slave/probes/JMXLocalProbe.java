/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import java.io.File;
import java.util.HashMap;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.monitor.slave.probes.mbean.BeanManager;

/**
 * @author ippoz
 *
 */
public class JMXLocalProbe extends CycleProbe {
	
	private BeanManager bManager;

	public JMXLocalProbe(String receiverIp, int probePort, File beanPrefFile) {
		super(LayerType.JVM, "JMX", receiverIp, probePort);
		try {
			bManager = new BeanManager();
			if(beanPrefFile.exists()){
				bManager.loadBeanPreferences(beanPrefFile);
			} else AppLogger.logError(getClass(), "FileNotFoundException", "Unable to find file for MBean preferences");
			bManager.writeBeansToFile("AvaliableBeans.csv");
		} catch (Exception ex){
			AppLogger.logException(getClass(), ex, "Error while Initializing MBeans");
		}
	}

	@Override
	protected HashMap<String, String> readParams() {
		return bManager.getObsValues();
	}

	@Override
	public void setupParameters() {
		// TODO
	}

	@Override
	public boolean canRun() {
		return true;
	}

}
