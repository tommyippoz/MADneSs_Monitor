/**
 * 
 */
package ippoz.madness.monitor.master;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.workload.SoapXmlWorkload;
import ippoz.multilayer.monitor.master.workload.Workload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class MasterManager.
 * Manager of the Master of the experiments.
 *
 * @author Tommy
 */
public class LiferayMasterManager extends MasterManager{

	public LiferayMasterManager(PreferencesManager pManager) throws IOException {
		super(pManager);
	}

	@Override
	protected LinkedList<Workload> readWorkloads(File detailsFile, File workloadFolder) {
		Workload currentWorkload;
		LinkedList<Workload> workloads = new LinkedList<Workload>();
		HashMap<String, HashMap<String, Integer>> workloadDetails = getWorkloadDetails(detailsFile);
		AppLogger.logOngoingInfo(getClass(), "Fetching workloads ");
		for(File wFile : workloadFolder.listFiles()){
			currentWorkload = null;
			try {
				if(wFile.getName().endsWith(".xml") && wFile.getName().toUpperCase().contains("WORKLOAD")){
					if(workloadDetails.get(wFile.getName()) != null)
						currentWorkload = new SoapXmlWorkload(wFile, null, workloadDetails.get(wFile.getName()).get("MIN_TIME"), workloadDetails.get(wFile.getName()).get("MAX_TIME"));
					else currentWorkload = new SoapXmlWorkload(wFile, null, 0, Integer.MAX_VALUE);;
				}
				if(currentWorkload != null){
					workloads.add(currentWorkload);
					System.out.print(".");
				} 
			} catch(Exception ex){
				AppLogger.logError(getClass(), ex.getClass().getName(), "Unable to load workload: " + wFile.getName());
			}	
		}
		System.out.println(" Available workloads: " + workloads.size());
		return workloads;
	}
	
	private HashMap<String, HashMap<String, Integer>> getWorkloadDetails(File workloadDetailFile) {
		String readed;
		String[] splitted;
		BufferedReader reader = null;
		HashMap<String, HashMap<String, Integer>> wlDetails = new HashMap<String, HashMap<String, Integer>>();
		try {
			reader = new BufferedReader(new FileReader(workloadDetailFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null && readed.length() > 0 && !readed.startsWith("workload_name")){
					splitted = readed.trim().split(",");
					wlDetails.put(splitted[0], new HashMap<String, Integer>());
					wlDetails.get(splitted[0]).put("MIN_TIME", Integer.parseInt(splitted[1]));
					wlDetails.get(splitted[0]).put("MAX_TIME", Integer.parseInt(splitted[2]));
				}
			}
			reader.close();	
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to load experiments");
		}
		return wlDetails;
	}
	
}