/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

import ippoz.multilayer.monitor.master.workload.JSeduiteWorkload;
import ippoz.multilayer.monitor.master.workload.Workload;

/**
 * @author ippoz
 *
 */
public class JSeduiteService extends RemoteService {

	private String details;
	
	public JSeduiteService(String name, String details) {
		super(name);
		this.details = details;
	}
	
	public String generateCompleteURL(String serverIP){
		return "http://" + serverIP + ":8080/" + details + "?wsdl";
	}

	@Override
	public boolean canTestWith(Workload workload) {
		String nUpper;
		if(workload instanceof JSeduiteWorkload){
			nUpper = workload.getName().toUpperCase();
			if(nUpper.equals(getName().toUpperCase()) || (nUpper.contains(".") && nUpper.split("\\.")[0].equals(getName().toUpperCase()))){
				return true;
			} else return false;
		}
		else return false;
	}

	@Override
	public String toString() {
		return "JSeduiteService: " + getName();
	}

}
