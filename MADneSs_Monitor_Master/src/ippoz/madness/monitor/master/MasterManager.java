/**
 * 
 */
package ippoz.madness.monitor.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.PreferencesManager;
import ippoz.madness.monitor.communication.CommunicationManager;
import ippoz.madness.monitor.communication.MessageType;
import ippoz.multilayer.monitor.master.database.DatabaseManager;
import ippoz.multilayer.monitor.master.experiment.Experiment;
import ippoz.multilayer.monitor.master.experiment.ExperimentType;
import ippoz.multilayer.monitor.master.experiment.Failure;
import ippoz.multilayer.monitor.master.workload.Workload;

/**
 * The Class MasterManager.
 * Manager of the Master of the experiments.
 *
 * @author Tommy
 */
public abstract class MasterManager {
	
	/** The database manager. */
	private DatabaseManager dbManager;
	
	/** The communication manager. */
	private CommunicationManager cManager;
	
	/** The preference manager. */
	private PreferencesManager prefManager;
	
	/** The experiment list. */
	private LinkedList<Experiment> expList;
	
	/** The test experiment list. */
	private LinkedList<Experiment> testList;
	
	/** The available workloads. */
	private LinkedList<Workload> availableWorkloads;
	
	/**
	 * Instantiates a new master manager.
	 *
	 * @param prefFile the preference file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MasterManager(File prefFile) throws IOException {
		this(new PreferencesManager(prefFile));
	}
	
	/**
	 * Instantiates a new master manager.
	 *
	 * @param prefManager the preference manager
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public MasterManager(PreferencesManager prefManager) throws IOException {
		this.prefManager = prefManager;
		dbManager = new DatabaseManager(prefManager, false);
		cManager = new CommunicationManager(prefManager.getPreference("SLAVE_IP_ADDRESS"), Integer.parseInt(prefManager.getPreference("OUT_PORT")), Integer.parseInt(prefManager.getPreference("IN_PORT")));
	}
	
	public boolean isInitialized() {
		return dbManager != null && cManager != null && dbManager.isAlive() && cManager.isAlive();
	}
	
	/**
	 * Setups the environment.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setupEnvironment() throws IOException {
		availableWorkloads = readWorkloads(new File(prefManager.getPreference("WORKLOAD_DETAILS")), new File(prefManager.getPreference("WORKLOAD_FOLDER")));
		setupExperiments();
	}
	
	/**
	 * Reads all the available workloads.
	 *
	 * @return the list of workloads
	 */
	protected abstract LinkedList<Workload> readWorkloads(File detailsFile, File workloadFolder);

	/**
	 * Reads the test and the golden/faulty experiments.
	 */
	private void setupExperiments(){
		File expFile = null;
		BufferedReader reader = null;
		String readed;
		try {
			expList = new LinkedList<Experiment>();
			testList = new LinkedList<Experiment>();
			expFile = new File(prefManager.getPreference("EXPERIMENT_FILE"));
			reader = new BufferedReader(new FileReader(expFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null && readed.length() > 0 && !readed.startsWith("workload_name"))
					parseExperiment(readed.trim());
			}
			reader.close();	
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to load experiments");
		}
	}
	
	/**
	 * Parses the experiment from a text file row.
	 *
	 * @param readed the read row of the text file (experiments separated by commas)
	 */
	private void parseExperiment(String readed){
		Experiment exp;
		String[] splitted = readed.split(",");
		if(splitted[0].endsWith(".xml")){
			if(ExperimentType.valueOf(readed.split(",")[1]) != ExperimentType.TEST) {
				if(splitted.length > 3){
					exp = new Experiment(getWorkloadByName(splitted[0]), ExperimentType.FAULTY, dbManager, Integer.parseInt(splitted[2]), parseFailures(readed));
				} else exp = new Experiment(getWorkloadByName(splitted[0]), ExperimentType.GOLDEN, dbManager, Integer.parseInt(splitted[2]), null);
				if(!exp.canExecute()){
					for(Experiment newExp : exp.getNeededTests(availableWorkloads, Integer.parseInt(prefManager.getPreference("TEST_ITERATIONS")))){
						if(!isInTestList(newExp))
							testList.add(newExp);
					}
				}
				expList.add(exp);
				AppLogger.logInfo(getClass(), "Readed '"+ exp.getExpType().toString() + "' experiment: " + exp.getWorkload().getName());
			} else {} // TODO
		}
		System.out.print(".");
	}
	
	/**
	 * Gets the workload by name.
	 *
	 * @param wName the workload name
	 * @return the workload by name
	 */
	private Workload getWorkloadByName(String wName){
		for(Workload workload : availableWorkloads){
			if(workload.getName().toUpperCase().equals(wName.toUpperCase()))
				return workload.cloneWorkload();
		}
		return null;
	}
	
	/**
	 * Checks if the experiment is in test list.
	 *
	 * @param newExp the experiment
	 * @return true, if is in test list
	 */
	private boolean isInTestList(Experiment expToCheck) {
		if(expToCheck.getExpType() == ExperimentType.TEST){
			for(Experiment exp : testList){
				if(exp.getExpType() == ExperimentType.TEST && exp.getWorkload().getName().equals(expToCheck.getWorkload().getName()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Parses the failures from a portion of text file row.
	 * Failures are separated by commas, fields are separated by semicolons.
	 *
	 * @param readed the read portion of file row
	 * @return the hash map
	 */
	private HashMap<Failure, Long> parseFailures(String readed) {
		String[] splitted = readed.split(",");
		String[] failureData;
		HashMap<Failure, Long> failMap = new HashMap<Failure, Long>();
		try {
			for(int i=7;i<splitted.length;i++){
				if(splitted[i].contains("#")) {
					failureData = splitted[i].split("#")[0].trim().split(";");
					failMap.put(new Failure(failureData[0], failureData[1], splitted[i].trim().substring(splitted[i].trim().indexOf("#")+1)), Long.valueOf(failureData[2]));
				} else {
					failureData = splitted[i].trim().split(";");
					failMap.put(new Failure(failureData[0], failureData[1], ""), Long.valueOf(failureData[2])); 
				}
			}
		} catch (Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to parse Fault details");
		}
		return failMap;
	}
	
	/**
	 * Checks the NTP synchronisation. If the difference is over 1 second, the check fails.
	 *
	 * @param response the response
	 * @param beforeMillis the machine time milliseconds
	 * @return true, if the clocks are synchronised
	 */
	private boolean checkNTP(LinkedList<Object> response, long beforeMillis){
		if(((MessageType)response.get(0)).equals(MessageType.OK)){
			if(response.size() == 2){
				if(((Long)response.get(1))/1000 >= beforeMillis/1000 && ((Long)response.get(1))/1000 <= System.currentTimeMillis()/1000){
					AppLogger.logInfo(getClass(), "NTP Synchronization checked");
					return true;
				} else {
					AppLogger.logError(getClass(), "UnSynchronizedClocksException", "NTP clocks are not synchronized");
					return false;
				}
			} else {
				AppLogger.logInfo(getClass(), "Unable to check NTP clock alignment");
				return true;
			}			
		} else {
			AppLogger.logError(getClass(), "WrongResponseException", "Unable to initialize campaign: wrong response");
			return false;
		}
	}

	/**
	 * Setups the experimental campaign.
	 *
	 * @return true, if the setup has success
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean setupCampaign() throws IOException {
		long beforeMillis;
		LinkedList<Object> response;
		cManager.send(new Object[]{MessageType.SETUP_SUT, "LiferaySUT"});
		cManager.waitForConfirm();
		cManager.send(MessageType.START_CAMPAIGN);
		beforeMillis = System.currentTimeMillis();
		response = cManager.receive();
		return checkNTP(response, beforeMillis);
	}
	
	/**
	 * Shutdowns the experimental campaign.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void shutdownCampaign() throws IOException {
		cManager.send(MessageType.END_CAMPAIGN);
		cManager.waitForConfirm();
	}
	
	/**
	 * Starts the experimental campaign.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void startExperimentalCampaign() throws IOException {
		if(setupCampaign()){
			executeExperiments(testList, "test");
			executeExperiments(expList, "experiment");
		}
		shutdownCampaign();
	}
	
	/**
	 * Execute a list of experiments.
	 *
	 * @param currentList the current list of experiments
	 * @param tag the experiments' tag
	 */
	private void executeExperiments(LinkedList<Experiment> currentList, String tag){
		int i = 1;
		for(Experiment currentExp : currentList){
			AppLogger.logInfo(getClass(), "Executing " + tag + " " + i + "/" + currentList.size() + ": " + currentExp.getExpType() + " Repeated " + currentExp.getIterations() + " times");
			currentExp.executeExperiment(cManager);
			currentExp = null;
			i++;
		}
	}
	
	/**
	 * Flushes the manager (database and communication managers).
	 *
	 * @throws SQLException the SQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void flush() throws SQLException, IOException {
		dbManager.flush();
		cManager.flush();
	}

}