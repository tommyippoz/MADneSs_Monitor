/**
 * 
 */
package ippoz.madness.monitor.slave.sut.injection;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class EnvInjection.
 *
 * @author root
 */
public class EnvInjection extends Injection {
	
	/** The filename for setting the environment variables. */
	private String setenvFilename;
	
	/** The log filename. */
	private String logFilename;

	/**
	 * Instantiates a new environment injection.
	 *
	 * @param injTag the inj tag
	 * @param delay the delay
	 * @param setenvFilename the set environment filename
	 * @param logFilename the log filename
	 * @param envVar the environment variables
	 */
	public EnvInjection(String injTag, long delay, String setenvFilename, String logFilename, String[] envVar) {
		super(injTag, delay);
		this.setenvFilename = setenvFilename;
		this.logFilename = logFilename;
		updateSetenvFile(envVar);
	}
	
	/**
	 * Read startup file.
	 *
	 * @return the linked list
	 */
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
	
	/**
	 * Update set environment file.
	 *
	 * @param envVar the environment variables
	 */
	private void updateSetenvFile(String[] envVar){
		BufferedWriter writer;
		LinkedList<String> startupRows = readStartupFile();
		try {
			writer = new BufferedWriter(new FileWriter(new File(setenvFilename)));
			writer.write("export JSEDUITE_CLEAN_VARIABLES=1\n");
			writer.write("export JSEDUITE_START_TIME=$(date +%s)\n");
			writer.write("export JSEDUITE_WORKLOAD_WAIT=" + (30+delay/1000) + "\n");
			writer.write("export FAILURE_DURATION=2\n\n");
			for(String env : envVar) {
				writer.write("export " + env + "=1\n");
			}
			writer.write("\n");
			for(String row : startupRows){
				if(!row.toUpperCase().contains("JSEDUITE") && !row.toUpperCase().contains("FAILURE")  && !row.toUpperCase().contains("INJ") && row.length() > 0) {
					writer.write(row + "\n");
				}
			}
			writer.close();
		} catch(Exception ex){
			
		}
	}
	
	/*private void updateSetenvFilez(String[] envVar){
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
	}*/

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.injection.Injection#inject()
	 */
	@Override
	protected void inject() {
		AppLogger.logInfo(getClass(), "Injected '" + toString() + "'");
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.sut.injection.Injection#flush()
	 */
	@Override
	protected void flush() {
		setInjTimestamp(getInjActivationTime(), getInjActivationDetail());
	}
	
	/**
	 * Gets the injection activation time.
	 *
	 * @return the injection activation time
	 */
	private Long getInjActivationTime(){
		long outTime = 0;
		BufferedReader reader;
		String readed;
		try {
			if(new File(logFilename).exists()){
				reader = new BufferedReader(new FileReader(new File(logFilename)));
				while(reader.ready()){
					readed = reader.readLine().trim();
					if(readed.length() > 0){
						outTime = AppUtility.parseStringTime(readed.split(";")[0], 0);
					}
				}
				reader.close();
			} else return null;
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to read Injection Log File");
		}
		return outTime;
	}
	
	/**
	 * Gets the injection activation detail.
	 *
	 * @return the injection activation detail
	 */
	private String getInjActivationDetail(){
		String outDetail = "";
		BufferedReader reader;
		String readed;
		try {
			if(new File(logFilename).exists()){
				reader = new BufferedReader(new FileReader(new File(logFilename)));
				while(reader.ready()){
					readed = reader.readLine().trim();
					if(readed.length() > 0){
						outDetail = readed.split(";")[1].trim();
					}
				}
				reader.close();
			} else return null;
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to read Injection Log File");
		}
		return outDetail;
	}

}
