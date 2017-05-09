/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.PreferencesManager;
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
		// TODO 
		return null;
	}

	@Override
	protected Workload createWorkload(File wFile, HashMap<String, HashMap<String, Integer>> workloadDetails) {
		// TODO 
		return null;
	}
	
}