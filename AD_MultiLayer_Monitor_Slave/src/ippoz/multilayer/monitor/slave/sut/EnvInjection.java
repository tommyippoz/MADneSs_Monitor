/**
 * 
 */
package ippoz.multilayer.monitor.slave.sut;

import ippoz.multilayer.monitor.support.AppLogger;
import ippoz.multilayer.monitor.support.AppUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author root
 *
 */
public class EnvInjection extends Injection {
	
	private String setenvFilename;
	private String logFilename;

	public EnvInjection(String injType, String injTag, long delay, String setenvFilename, String logFilename, String[] envVar) {
		super(injType, injTag, delay);
		this.setenvFilename = setenvFilename;
		this.logFilename = logFilename;
		updateSetenvFile(envVar);
	}
	
/*	export injAD=1
			export injEK=1
			export injTSM=1
			export LIFERAY_CLEAN_VARIABLES=1
			export LIFERAY_START_TIME=$(date +%s)
			export LIFERAY_WORKLOAD_WAIT=1
			export FAILURE_DURATION=5
			export FAILURE_MEMORY=1 */
	
	private LinkedList<String> readStartupFile(){
		BufferedReader reader;
		LinkedList<String> startupRows = new LinkedList<String>();
		try {
			reader = new BufferedReader(new FileReader(new File(setenvFilename)));
			while(reader.ready()){
				startupRows.add(reader.readLine());
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read startup file");
		}
		return startupRows;
	}
	
	private void updateSetenvFile(String[] envVar){
		BufferedWriter writer;
		LinkedList<String> startupRows = readStartupFile();
		try {
			writer = new BufferedWriter(new FileWriter(new File(setenvFilename)));
			writer.write("export LIFERAY_CLEAN_VARIABLES=1\n");
			writer.write("export LIFERAY_START_TIME=$(date +%s)\n");
			writer.write("export LIFERAY_WORKLOAD_WAIT=" + (120+delay/1000) + "\n");
			writer.write("export FAILURE_DURATION=5\n\n");
			for(String env : envVar) {
				writer.write("export " + env + "=1\n");
			}
			writer.write("\n");
			for(String row : startupRows){
				writer.write(row + "\n");
			}
			writer.close();
		} catch(Exception ex){
			
		}
	}
	
	private void updateSetenvFilez(String[] envVar){
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(setenvFilename)));
			writer.write("export LIFERAY_CLEAN_VARIABLES=1\n");
			writer.write("export LIFERAY_START_TIME=$(date +%s)\n");
			writer.write("export LIFERAY_WORKLOAD_WAIT=" + (120+delay/1000) + "\n");
			writer.write("export FAILURE_DURATION=5\n\n");
			for(String env : envVar) {
				writer.write("export " + env + "=1\n");
			}
			writer.close();
		} catch(Exception ex){
			
		}
	}

	@Override
	protected void inject() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void flush() {
		setInjTimestamp(getLogInjTime());
	}
	
	private long getLogInjTime(){
		long outTime = 0;
		BufferedReader reader;
		String readed;
		try {
			reader = new BufferedReader(new FileReader(new File(logFilename)));
			while(reader.ready()){
				readed = reader.readLine().trim();
				if(readed.length() > 0){
					outTime = AppUtility.parseStringTime(readed.split(";")[0], 1);
				}
			}
			reader.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to read Injection Log File");
		}
		return outTime;
	}

}
