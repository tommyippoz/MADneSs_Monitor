/**
 * 
 */
package ippoz.multilayer.monitor.master.database;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;
import ippoz.madness.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.experiment.ExperimentType;
import ippoz.multilayer.monitor.master.experiment.Failure;
import ippoz.multilayer.monitor.master.experiment.ServiceTestExperiment;
import ippoz.multilayer.monitor.master.observation.Observation;
import ippoz.multilayer.monitor.master.observation.ObservationCollector;
import ippoz.multilayer.monitor.master.performance.ExperimentTiming;
import ippoz.multilayer.monitor.master.performance.ObservationTiming;
import ippoz.multilayer.monitor.master.probes.receiver.ProbeReceiver;
import ippoz.multilayer.monitor.master.services.Invocation;
import ippoz.multilayer.monitor.master.services.MethodCall;
import ippoz.multilayer.monitor.master.services.RemoteService;
import ippoz.multilayer.monitor.master.services.Service;
import ippoz.multilayer.monitor.master.workload.Workload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Tommy
 *
 */
public class DatabaseManager {
	
	private static final String DB_USERNAME = "DB_USERNAME";
	private static final String DB_PASSWORD = "DB_PASSWORD";
	private static final String DB_NAME = "DB_NAME";
	
	private DatabaseConnector connector;
	private HashMap<ExperimentType, String> expTypes;
	private HashMap<LayerType, String> layerTypes;
	private HashMap<String, String> failureTypes;
	private HashMap<String, String> valueCategories;
	private HashMap<String, String> indicators;
	private HashMap<String, String> performanceTypes;
	
	public DatabaseManager(PreferencesManager prefManager, boolean create){
		try {
			if(prefManager.hasPreference(DB_USERNAME) && prefManager.hasPreference(DB_PASSWORD) && prefManager.hasPreference(DB_NAME)){
				connector = new DatabaseConnector(prefManager.getPreference(DB_NAME), prefManager.getPreference(DB_USERNAME), prefManager.getPreference(DB_PASSWORD), create);
				updateInfo();
			} else {
				AppLogger.logError(getClass(), "DbPrefError", "Database Preferences not specified correctly");
			}
		} catch(Exception ex){
			AppLogger.logInfo(getClass(), "Need to start MySQL Server...");
		}
	}
	
	public boolean isAlive() {
		return connector.isAlive();
	}

	private void updateInfo() {
		setExperimentTypes();
		setFailureTypes();
		setLayerType();
		setIndicatorList();
		setValueCategories();
		setPerformanceTypes();
	}

	private void setValueCategories() {
		valueCategories = new HashMap<String, String>();
		for(HashMap<String, String> vCategory : connector.executeCustomQuery(null, "select * from value_category")){
			valueCategories.put(vCategory.get("vc_description"), vCategory.get("value_category_id"));
		}
	}

	private void setPerformanceTypes() {
		performanceTypes = new HashMap<String, String>();
		for(HashMap<String, String> pType : connector.executeCustomQuery(null, "select * from performance_type")){
			performanceTypes.put(pType.get("pet_description"), pType.get("performance_type_id"));
		}
	}
	
	private void setFailureTypes() {
		failureTypes = new HashMap<String, String>();
		for(HashMap<String, String> failure : connector.executeCustomQuery(null, "select * from failure_type")){
			failureTypes.put(failure.get("ft_description"), failure.get("failure_type_id"));
		}
	}

	private void setIndicatorList() {
		indicators = new HashMap<String, String>();
		for(HashMap<String, String> indicator : connector.executeCustomQuery(null, "select indicator_id, in_tag from indicator")){
			indicators.put(indicator.get("in_tag"), indicator.get("indicator_id"));
		}
	}

	private void setLayerType() {
		layerTypes = new HashMap<LayerType, String>();
		for(HashMap<String, String> layer : connector.executeCustomQuery(null, "select * from probe_type")){
			layerTypes.put(LayerType.valueOf(layer.get("pt_description").toUpperCase()), layer.get("probe_type_id"));
		}
	}

	private void setExperimentTypes() {
		expTypes = new HashMap<ExperimentType, String>();
		for(HashMap<String, String> layer : connector.executeCustomQuery(null, "select * from run_type")){
			expTypes.put(ExperimentType.valueOf(layer.get("rt_description").toUpperCase()), layer.get("run_type_id"));
		}
	}

	public void newExperiment(ObservationCollector obCollector, ExperimentType expType, Workload workload) {
		String workloadID = getWorkloadID(workload, true);
		connector.update("insert into run (run_type_id, workload_id) values (" + expTypes.get(expType) + ", " + workloadID + ")");
		obCollector.setRunId(DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select max(run_id) as maxrun from run"), "maxrun"));
	}
	
	public void undoExperiment() {
		connector.update("delete from run order by run_id desc limit 1");
	}
	
	private String getWorkloadID(Workload workload, boolean insertFlag) {
		String sid;
		int i = 1;
		String wid = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select workload_id from workload where wl_name = '" + workload.getName() + "'"), "workload_id"); 
		if((wid == null || wid.length() == 0) && insertFlag){
			connector.update("insert into workload (wl_name) values('" + workload.getName() + "')");
			wid = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select workload_id from workload where wl_name = '" + workload.getName() + "'"), "workload_id");
			for(Service service : workload.usedServices()){
				sid = getServiceId(service);
				connector.update("insert into workload_service (workload_id, service_id, ws_index) values (" + wid + ", " + sid + ", " + i + ")");
				i++;
			}
		}
		return wid;
	}

	public void flush() throws SQLException {
		connector.closeConnection();
	}
	
	public void storeObservation(Observation current, Observation prevoius, String runId){
		storeIndicatorObservation(current, runId);
		storeIndicatorObservationValues(current, prevoius, runId);
	}
	
	private void storeIndicatorObservationValues(Observation current, Observation previous, String runId) {
		String query = "insert into indicator_observation_category (indicator_observation_id, value_category_id, ioc_value) values ";
		for(HashMap<String, String> ioMap : connector.executeCustomQuery(null, "select in_tag, indicator_observation_id from indicator_observation natural join indicator natural join observation where ob_time = '" + current.getTimestamp() + "' order by indicator_observation_id asc" )){
			if(query.endsWith(")"))
				query = query + ", ";
			query = query + "(" + ioMap.get("indicator_observation_id") + ", " + valueCategories.get("Plain") + ", '" + current.getValue(ioMap.get("in_tag")) + "')";
			query = query + ", (" + ioMap.get("indicator_observation_id") + ", " + valueCategories.get("Difference") + ", '" + getDifferenceBetween(current, previous, ioMap.get("in_tag")) + "')";
			
		}
		connector.update(query);
	}
	
	private static String getDifferenceBetween(Observation current, Observation previous, String indName) {
		if(current != null && previous != null)
			return String.valueOf(Double.valueOf(current.getValue(indName)) - Double.valueOf(previous.getValue(indName)));
		else return "0";
	}

	private void storeIndicatorObservation(Observation current, String runId){
		String obsId, indId;
		String indUpdateQuery = "insert into indicator_observation (indicator_id, observation_id) values "; 
		if(current != null && runId != null) {
			connector.update("insert into observation (run_id, ob_time) values (" + runId + ", '" + current.getTimestamp() + "')");
			obsId = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select max(observation_id) as maxobs from observation"), "maxobs");
			for(Indicator ind : current.getIndicators()){
				indId = indicators.get(ind.getName());
				if(indId == null){
					connector.update("insert into indicator (probe_type_id, in_tag, in_description) values (" + layerTypes.get(ind.getLayer()) + ", '" + ind.getName() + "', '')");
					indicators.put(ind.getName(), DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select indicator_id from indicator where in_tag = '" + ind.getName() + "'"), "indicator_id"));
					indId = indicators.get(ind.getName());
				}
				if(indUpdateQuery.endsWith(")"))
					indUpdateQuery = indUpdateQuery + ", ";
				indUpdateQuery = indUpdateQuery + "(" + indId + ", " + obsId + ")";
			}
			connector.update(indUpdateQuery);
		} else AppLogger.logError(getClass(), "NullPointerException", "Received a null parameter");
	}
	
	private String getServiceId(Service service){
		return DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select service_id from service where se_name='" + service.getName() + "'"), "service_id");
	}
	
	private void addMethod(String serviceId, MethodCall methodCall, int index){
		String methodId;
		connector.update("insert into method (me_name, me_portlet) values "
				+ "('" + methodCall.getMethod() + "', '" + methodCall.getPortlet() + "')");
		methodId = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select method_id from method where me_name='" + methodCall.getMethod() + "' and me_portlet='" + methodCall.getPortlet() + "'"), "method_id");
		connector.update("insert into service_method (method_id, service_id, me_index) values (" + methodId + ", " + serviceId + ", " + index + ")");
	}
	
	private String addService(Service service){
		RemoteService rService;
		String serviceId = getServiceId(service);
		if(serviceId == null) {
			connector.update("insert into service (se_name) values('" + service.getName() + "')");
			serviceId = getServiceId(service);
			if(service instanceof RemoteService){
				rService = (RemoteService)service;
				for(int i=0;i<rService.getMethodNumber();i++){
					addMethod(serviceId, rService.getMethodCall(i), (i+1));
				}
			}
		}
		return serviceId;
	}
	
	public boolean hasService(Service service, boolean addIfMissing){
		if(getServiceId(service) == null){
			if(addIfMissing) {
				addService(service);
				return true;
			} else return false;
		} else return true;
	}
	
	public boolean hasStat(Service service) {
		String serviceId;
		serviceId = getServiceId(service);
		if(serviceId == null)
			serviceId = addService(service);
		if(connector.executeCustomQuery(null, "select service_stat_id from service_stat natural join service where service_id = " + serviceId).size() > 0)
			return true;
		else return false;
	}

	public void logExperimentInfo(Workload workload) {
		String runId, smId;
		String query = "insert into service_method_invocation (run_id, service_method_id, start_time, end_time, response) values ";
		runId = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select max(run_id) as maxRun from run"), "maxRun");
		for(Invocation invocation : workload.executedInvocations()){
			smId = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select service_method_id from service natural join service_method natural join method where se_name='" + invocation.getService().getName() + "' and me_name='" + invocation.getMethod() + "'"), "service_method_id");
			if(query.endsWith(")"))
				query = query + ", ";
			query = query + "(" + runId + ", " + smId + ", '" + AppUtility.formatMillis(invocation.getStartTime()) + "', '" + AppUtility.formatMillis(invocation.getEndTime()) + "', '" + invocation.getResponseCode() + "')";
		}
		connector.update(query);
	}

	public LinkedList<Observation> getRunObservations(String runId, String startTime, String endTime, String categoryTag) {
		Observation obs;
		LinkedList<Observation> obsList = new LinkedList<Observation>();
		for(HashMap<String, String> obsMap : connector.executeCustomQuery(null, "select observation_id, ob_time from observation where run_id = " + runId + " and (ob_time between '" + startTime + "' and '" + endTime + "')")){
			obs = new Observation(AppUtility.getDateFromString(obsMap.get("ob_time")), obsMap.get("ob_time"));
			for(HashMap<String, String> indObs : connector.executeCustomQuery(null, "select in_tag, ioc_value from indicator natural join indicator_observation natural join indicator_observation_category where observation_id = " + obsMap.get("observation_id") + " and value_category_id = " + valueCategories.get(categoryTag))) {
				obs.addIndicator(new Indicator(indObs.get("in_tag"), LayerType.JVM, String.class), indObs.get("ioc_value"));
			}
			obsList.add(obs);
		}
		return obsList;
	}

	public ArrayList<HashMap<String, String>> getRunIdList(Service service, int testIterations) {
		return connector.executeCustomQuery(null, "select run_id, min(start_time) as start_time, max(end_time) as end_time from service_method_invocation natural join service_method natural join service where se_name = '" + service.getName() + "' group by run_id order by run_id desc limit " + testIterations);
	}

	public void writeServiceStat(Service service, HashMap<String, LinkedList<HashMap<String, String>>> indicatorStats, HashMap<String, String> serviceStats) {
		String isQuery;
		String servStatID;
		connector.update("insert into service_stat (service_id, serv_dur_avg, serv_dur_std, serv_dur_std_perc, serv_obs_avg, serv_obs_std, serv_obs_std_perc) values (" + getServiceId(service) + ", " 
				+ serviceStats.get(ServiceTestExperiment.DURATION_AVG) + ", " + serviceStats.get(ServiceTestExperiment.DURATION_STD) + ", " + serviceStats.get(ServiceTestExperiment.DURATION_STD_PERC) + ", "
				+ serviceStats.get(ServiceTestExperiment.OBSERVATION_AVG) + ", " + serviceStats.get(ServiceTestExperiment.OBSERVATION_STD) + ", " + serviceStats.get(ServiceTestExperiment.OBSERVATION_STD_PERC) + ")");
		servStatID = DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select max(service_stat_id) as max_ss_id from service_stat"), "max_ss_id");
		for(String categoryTag : indicatorStats.keySet()){
			isQuery = "insert into service_indicator_stat (service_stat_id, indicator_id, value_category_id, si_avg_first, si_med_first, si_mod_first, si_std_first, si_avg_last, si_med_last, si_mod_last, si_std_last, si_all_avg, si_all_med, si_all_mod, si_all_std) values ";
			for(HashMap<String, String> iStat : indicatorStats.get(categoryTag)) {
				if(isQuery.endsWith(")"))
					isQuery = isQuery + ", "; 
				isQuery = isQuery + "(" + servStatID + ", " + indicators.get(iStat.get(ServiceTestExperiment.IS_NAME)) + ", " + valueCategories.get(categoryTag) + ", " 
					+ iStat.get(ServiceTestExperiment.IS_AVG_FIRST) + ", " + iStat.get(ServiceTestExperiment.IS_MED_FIRST) + ", " + iStat.get(ServiceTestExperiment.IS_MOD_FIRST) + ", " + iStat.get(ServiceTestExperiment.IS_STD_FIRST) + ", " 
					+ iStat.get(ServiceTestExperiment.IS_AVG_LAST) + ", " + iStat.get(ServiceTestExperiment.IS_MED_LAST) + ", " + iStat.get(ServiceTestExperiment.IS_MOD_LAST) + ", " + iStat.get(ServiceTestExperiment.IS_STD_LAST) + ", " 
					+ iStat.get(ServiceTestExperiment.IS_ALL_AVG) + ", " + iStat.get(ServiceTestExperiment.IS_ALL_MED) + ", " + iStat.get(ServiceTestExperiment.IS_ALL_MOD) + ", " + iStat.get(ServiceTestExperiment.IS_ALL_STD) + ")";	
			}
			connector.update(isQuery);
		}
	}

	public void storeInjActivations(Set<Failure> failures, String runId) {
		String toSave = "";
		for(Failure failure : failures){
			if(failure.getFailureActivation() != null){
				toSave = toSave + " (" + failureTypes.get(failure.getFailureType()) + ", " + runId + ", '" + failure.getFailureTag() + "', '" + AppUtility.formatMillis(failure.getFailureActivation()) + "'),";
			}
		}
		if(toSave.length() > 0)
			connector.update("insert into failure (failure_type_id, run_id, fa_description, fa_time) values" + toSave.substring(0, toSave.length()-1));
	}

	public Set<String> getDataCategoryTags() {
		return valueCategories.keySet();
	}

	public void saveMonitorPerformance(String runId) {
		String toSave = "";
		HashMap<ProbeReceiver, LinkedList<ObservationTiming>> tList = ExperimentTiming.getTimings();
		for(ProbeReceiver receiver : tList.keySet()){
			for(ObservationTiming oTiming : tList.get(receiver)){
				toSave = toSave + " (" +  runId + ", " + layerTypes.get(receiver.getLayerType()) + ", " + performanceTypes.get("ot") + ", " + oTiming.getIndNumber() + ", " + oTiming.getOt() + "),";
				toSave = toSave + " (" +  runId + ", " + layerTypes.get(receiver.getLayerType()) + ", " + performanceTypes.get("pmtt") + ", " + oTiming.getIndNumber() + ", " + oTiming.getPmtt() + "),";
				toSave = toSave + " (" +  runId + ", " + layerTypes.get(receiver.getLayerType()) + ", " + performanceTypes.get("dat") + ", " + oTiming.getIndNumber() + ", " + oTiming.getDat() + "),";
			}
		}
		if(toSave.length() > 0)
			connector.update("insert into performance (run_id, probe_type_id, performance_type_id, ind_number, perf_time) values " + toSave.substring(0,  toSave.length()-1));
	}

}
