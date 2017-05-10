/**
 * 
 */
package ippoz.multilayer.monitor.master.workload;

import java.io.File;

/**
 * The Class FileWorkload.
 * Defines a workload that is described in a single file.
 *
 * @author Tommy
 */
public abstract class FileWorkload extends Workload {
	
	/** The workload file. */
	protected File workloadFile;
	
	/**
	 * Instantiates a new file workload.
	 *
	 * @param file the file of the workload
	 * @param minTime the minimum execution time
	 * @param maxTime the maximum execution time
	 */
	public FileWorkload(File file, int minTime, int maxTime){
		super(file.getName(), minTime, maxTime);
		workloadFile = file;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#getName()
	 */
	@Override
	public String getName() {
		return workloadFile.getName();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#isValid()
	 */
	@Override
	public boolean isValid() {
		return workloadFile.exists();
	}
	
}
