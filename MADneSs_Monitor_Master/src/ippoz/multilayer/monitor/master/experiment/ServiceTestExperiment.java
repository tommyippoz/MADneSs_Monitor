/**
 * 
 */
package ippoz.multilayer.monitor.master.experiment;

import java.util.HashMap;
import java.util.LinkedList;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;
import ippoz.madness.monitor.communication.CommunicationManager;
import ippoz.multilayer.monitor.master.database.DatabaseManager;
import ippoz.multilayer.monitor.master.observation.Observation;
import ippoz.multilayer.monitor.master.services.Service;
import ippoz.multilayer.monitor.master.workload.Workload;

/**
 * @author Tommy
 *
 */
public class ServiceTestExperiment extends Experiment {

	public static final String OBSERVATION_AVG = "obs_avg";
	public static final String OBSERVATION_STD = "obs_std";
	public static final String OBSERVATION_STD_PERC = "obs_std_perc";
	public static final String DURATION_AVG = "dur_avg";
	public static final String DURATION_STD = "dur_std";
	public static final String DURATION_STD_PERC = "dur_std_perc";
	
	public static final String IS_AVG_FIRST = "is_avg_first";
	public static final String IS_MED_FIRST = "is_med_first";
	public static final String IS_MOD_FIRST = "is_mod_first";
	public static final String IS_STD_FIRST = "is_std_first";
	public static final String IS_AVG_LAST = "is_avg_last";
	public static final String IS_MED_LAST = "is_med_last";
	public static final String IS_MOD_LAST = "is_mod_last";
	public static final String IS_STD_LAST = "is_std_last";
	public static final String IS_ALL_AVG = "is_avg";
	public static final String IS_ALL_MED = "is_med";
	public static final String IS_ALL_MOD = "is_mod";
	public static final String IS_ALL_STD = "is_std";
	public static final String IS_NAME = "is_name";
	
	private Service service;
	
	public ServiceTestExperiment(Service service, LinkedList<Workload> availableWorkloads, DatabaseManager dbManager, int iterations) {
		super(findTestWorkload(service, availableWorkloads), ExperimentType.TEST, dbManager, iterations, null);
		this.service = service;
	}
	
	public static Workload findTestWorkload(Service service, LinkedList<Workload> availableWorkloads){
		for(Workload current : availableWorkloads){
			if(service.canTestWith(current))
				return current;
		}
		AppLogger.logError(AppUtility.class, "NoSuchWorkload", "Unable to find test workload for service " + service.getName());
		return null;
	}

	@Override
	public void executeExperiment(CommunicationManager cManager) {
		super.executeExperiment(cManager);
		calculateServiceStats();
	}	

	private void calculateServiceStats() {
		HashMap<String, LinkedList<HashMap<String, String>>> indicatorStat = new HashMap<String, LinkedList<HashMap<String, String>>>();
		LinkedList<LinkedList<Observation>> runObsList = null;
		for(String categoryTag : dbManager.getDataCategoryTags()){
			runObsList = new LinkedList<LinkedList<Observation>>();
			for(HashMap<String, String> runInfo : dbManager.getRunIdList(service, getIterations())){
				runObsList.add(dbManager.getRunObservations(runInfo.get("run_id"), runInfo.get("start_time"), runInfo.get("end_time"), categoryTag));
			}
			indicatorStat.put(categoryTag, computeIndicatorStats(runObsList));
		}
		dbManager.writeServiceStat(service, indicatorStat, computeServiceStats(runObsList));
	}

	private HashMap<String, String> computeServiceStats(LinkedList<LinkedList<Observation>> runObsList) {
		HashMap<String, String> outMap = new HashMap<String, String>();
		LinkedList<Double> obsNumbers = new LinkedList<Double>();
		LinkedList<Double> durations = new LinkedList<Double>();
		long t1, t2;
		for(int i=0;i<runObsList.size();i++) {
			if(runObsList.get(i).size() > 0) {
				obsNumbers.add((double) runObsList.get(i).size());
				t1 = (AppUtility.getDateFromString(runObsList.get(i).getFirst().getTimestamp())).getTime();
				t2 = (AppUtility.getDateFromString(runObsList.get(i).getLast().getTimestamp())).getTime();
				durations.add((double) (((int)(t2-t1))/1000.0));
			}
		}
		outMap.put(OBSERVATION_AVG, String.valueOf(AppUtility.calcAvg(obsNumbers.toArray(new Double[obsNumbers.size()]))));
		outMap.put(OBSERVATION_STD, String.valueOf(AppUtility.calcStd(obsNumbers.toArray(new Double[obsNumbers.size()]), AppUtility.calcAvg(obsNumbers.toArray(new Double[obsNumbers.size()])))));
		outMap.put(OBSERVATION_STD_PERC, String.valueOf(Double.valueOf(outMap.get(OBSERVATION_STD))*100.0/Double.parseDouble(outMap.get(OBSERVATION_AVG))));
		outMap.put(DURATION_AVG, String.valueOf(AppUtility.calcAvg(durations.toArray(new Double[durations.size()]))));
		outMap.put(DURATION_STD, String.valueOf(AppUtility.calcStd(durations.toArray(new Double[durations.size()]), AppUtility.calcAvg(durations.toArray(new Double[durations.size()])))));
		outMap.put(DURATION_STD_PERC, String.valueOf(Double.valueOf(outMap.get(DURATION_STD))*100.0/Double.parseDouble(outMap.get(DURATION_AVG))));
		return outMap;
		
	}

	private LinkedList<HashMap<String, String>> computeIndicatorStats(LinkedList<LinkedList<Observation>> runObsList) {
		LinkedList<Double> first;
		LinkedList<Double> last;
		LinkedList<Double> all;
		HashMap<String, String> indStat;
		LinkedList<HashMap<String, String>> outList = new LinkedList<HashMap<String, String>>();
		while(runObsList.size() > 0 && runObsList.getFirst().size() == 0){
			runObsList.removeFirst();
		}
		if(runObsList.size() > 0){
			for(Indicator ind : runObsList.getFirst().getFirst().getIndicators()){
				if(isNumber(runObsList.getFirst().getFirst().getValue(ind))) {
					first = new LinkedList<Double>();
					last = new LinkedList<Double>();
					all = new LinkedList<Double>();
					indStat = new HashMap<String, String>();
					for(LinkedList<Observation> oList : runObsList){
						if(oList.size() > 0){
							first.add(Double.parseDouble(oList.getFirst().getValue(ind)));
							last.add(Double.parseDouble(oList.getLast().getValue(ind)));
							for(Observation ob : oList){
								all.add(Double.parseDouble(ob.getValue(ind)));
							}
						}
					}
					indStat.put(IS_NAME, ind.getName());
					indStat.put(IS_AVG_FIRST, String.valueOf(AppUtility.calcAvg(first.toArray(new Double[first.size()]))));
					indStat.put(IS_MED_FIRST, String.valueOf(AppUtility.calcMedian(first.toArray(new Double[first.size()]))));
					indStat.put(IS_MOD_FIRST, String.valueOf(AppUtility.calcMode(first.toArray(new Double[first.size()]))));
					indStat.put(IS_STD_FIRST, String.valueOf(AppUtility.calcStd(first.toArray(new Double[first.size()]), AppUtility.calcAvg(first.toArray(new Double[first.size()])))));
					indStat.put(IS_AVG_LAST, String.valueOf(AppUtility.calcAvg(last.toArray(new Double[last.size()]))));
					indStat.put(IS_MED_LAST, String.valueOf(AppUtility.calcMedian(last.toArray(new Double[last.size()]))));
					indStat.put(IS_MOD_LAST, String.valueOf(AppUtility.calcMode(last.toArray(new Double[last.size()]))));
					indStat.put(IS_STD_LAST, String.valueOf(AppUtility.calcStd(last.toArray(new Double[last.size()]), AppUtility.calcAvg(last.toArray(new Double[last.size()])))));
					indStat.put(IS_ALL_AVG, String.valueOf(AppUtility.calcAvg(all.toArray(new Double[all.size()]))));
					indStat.put(IS_ALL_MED, String.valueOf(AppUtility.calcMedian(all.toArray(new Double[all.size()]))));
					indStat.put(IS_ALL_MOD, String.valueOf(AppUtility.calcMode(all.toArray(new Double[all.size()]))));
					indStat.put(IS_ALL_STD, String.valueOf(AppUtility.calcStd(all.toArray(new Double[all.size()]), AppUtility.calcAvg(all.toArray(new Double[all.size()])))));
					outList.add(indStat);
				}
			}
		}		
		return outList;
	}
	
	private boolean isNumber(String string){
		try {
			Double.parseDouble(string);
			return true;
		} catch(Exception ex){
			return false;
		}
	}
	
	

}
