/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

import ippoz.multilayer.monitor.master.workload.SoapXmlWorkload;
import ippoz.multilayer.monitor.master.workload.Workload;


/**
 * @author Tommy
 *
 */
public class SoapXmlService extends RemoteService {

	public SoapXmlService(String name) {
		super(name);
	}
	
	@Override
	public boolean canTestWith(Workload workload) {
		if(workload instanceof SoapXmlWorkload){
			if(workload.getName().contains(getName() + ".")){
				return true;
			} else return false;
		}
		else return false;
	}

	@Override
	public String toString() {
		return "SoapXmlService: " + getName();
	}

}
