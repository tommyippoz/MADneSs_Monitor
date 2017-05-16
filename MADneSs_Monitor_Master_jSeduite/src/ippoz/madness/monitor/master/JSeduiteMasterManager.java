/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.workload.JSeduiteWorkload;
import ippoz.multilayer.monitor.master.workload.Workload;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * The Class MasterManager.
 * Manager of the Master of the experiments.
 *
 * @author Tommy
 */
public class JSeduiteMasterManager extends MasterManager {

	private String serverIP;
	
	public JSeduiteMasterManager(PreferencesManager pManager) throws IOException {
		super(pManager);
		serverIP = pManager.getPreference(SLAVE_IP);
	}

	@Override
	protected LinkedList<Workload> readWorkloads(File detailsFile, File workloadFolder) {
		File[] wFiles = null;
		LinkedList<Workload> wList;
		if(workloadFolder != null && workloadFolder.exists()){
			 if(workloadFolder.isDirectory()){
				 wFiles = workloadFolder.listFiles();
			 } else wFiles = new File[]{workloadFolder};
			 wList = new LinkedList<Workload>();
			 for(File wFile : wFiles){
				 if(isJSeduiteWorkload(wFile)){
					 wList.add(new JSeduiteWorkload(wFile.getName(), new PreferencesManager(wFile), serverIP));
				 }
			 }
			 return wList;
		} else return null;
	}

	private boolean isJSeduiteWorkload(File wFile) {
		return wFile.getName().endsWith(".jsw");
	}

	@Override
	protected String getSUTName() {
		return "JSeduiteSUT";
	}
	
}