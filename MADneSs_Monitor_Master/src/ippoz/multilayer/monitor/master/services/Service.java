/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

import ippoz.multilayer.monitor.master.database.DatabaseManager;
import ippoz.multilayer.monitor.master.workload.Workload;

/**
 * @author Tommy
 *
 */
public abstract class Service implements Comparable<Service> {
	
	private String name;
	
	public Service(String name){
		this.name = name;
		
	}
	
	public String getName(){
		return name;
	}	
	
	public abstract boolean canTestWith(Workload workload);
	
	public abstract String toString();
	
	public boolean needTest(DatabaseManager database){
		return !database.hasStat(this);
	}

	@Override
	public int compareTo(Service other) {
		return name.compareTo(other.getName());
	}
	

}
