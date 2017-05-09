/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.workload.SoapXmlWorkload;
import ippoz.multilayer.monitor.master.workload.Workload;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * The Class MasterManager.
 * Manager of the Master of the experiments.
 *
 * @author Tommy
 */
public class JSeduiteMasterManager extends MasterManager{

	public JSeduiteMasterManager(PreferencesManager pManager) throws IOException {
		super(pManager);
	}

	@Override
	protected Workload createDefaultWorkload(File wFile) {
		return new SoapXmlWorkload(wFile, null, 0, Integer.MAX_VALUE);
	}

	@Override
	protected Workload createWorkload(File wFile, HashMap<String, HashMap<String, Integer>> workloadDetails) {
		return new SoapXmlWorkload(wFile, null, workloadDetails.get(wFile.getName()).get("MIN_TIME"), workloadDetails.get(wFile.getName()).get("MAX_TIME"));
	}
	
}