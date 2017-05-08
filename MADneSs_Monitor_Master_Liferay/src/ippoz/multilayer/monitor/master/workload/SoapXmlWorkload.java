/**
 * 
 */
package ippoz.multilayer.monitor.master.workload;

import ippoz.multilayer.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.services.Invocation;
import ippoz.multilayer.monitor.master.services.MethodCall;
import ippoz.multilayer.monitor.master.services.RemoteService;
import ippoz.multilayer.monitor.master.services.Service;
import ippoz.multilayer.monitor.master.services.SoapXmlService;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import com.resiltech.testing_box.functional_tester.configuration.XmlWorkload;
import com.resiltech.testing_box.functional_tester.configuration.instructions.Call;
import com.resiltech.testing_box.functional_tester.runner.FunctionalTestRunner;

/**
 * The Class SoapXmlWorkload.
 * Defines a SOAP workload defined in an xml file.
 *
 * @author Tommy
 */
public class SoapXmlWorkload extends FileWorkload {
	
	/** The Constant SERVICE_NAME_TAG. */
	private static final String SERVICE_NAME_TAG = "SERVICE_NAME";
	
	/** The Constant METHOD_NAME_TAG. */
	private static final String METHOD_NAME_TAG = "METHOD_NAME";
	
	/** The Constant START_TIME_TAG. */
	private static final String START_TIME_TAG = "START_TIME";
	
	/** The Constant END_TIME_TAG. */
	private static final String END_TIME_TAG = "END_TIME";
	
	/** The Constant RESPONSE_CODE. */
	private static final String RESPONSE_CODE = "RESPONSE_CODE";
	
	/** The isRunning flag. */
	private boolean isRunning;
	
	/** The preferences manager. */
	private PreferencesManager preferences;
	
	/** The SOAP functional tester. */
	private FunctionalTestRunner soapTester;

	/**
	 * Instantiates a new soap xml workload.
	 *
	 * @param file the xml file
	 * @param preferences the preferences manager
	 */
	public SoapXmlWorkload(File file, PreferencesManager preferences, int minTime, int maxTime) {
		super(file, minTime, maxTime);
		this.preferences = preferences;
		soapTester = new FunctionalTestRunner(file);
		isRunning = false;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#cloneWorkload()
	 */
	@Override
	public Workload cloneWorkload() {
		return new SoapXmlWorkload(workloadFile, preferences, getMinExecutionTime(), getMaxExecutionTime());
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#usedServices()
	 */
	@Override
	public LinkedList<Service> usedServices() {
		RemoteService newService;
		LinkedList<Service> servicesList = new LinkedList<Service>();
		for(XmlWorkload wLoad : soapTester.listWorkloads()){
			newService = new SoapXmlService(wLoad.getName());
			if(!existsIn(newService, servicesList)){
				for(Call call : wLoad.getCalls()){
					newService.addMethodCall(new MethodCall(call.get_portlet(), call.get_method()));
				}
				servicesList.add(newService);
			}
		}
		return servicesList;
	}
	
	/**
	 * Checks if the service exists in the list.
	 *
	 * @param service the service
	 * @param serviceList the service list
	 * @return true, if the service exists in the list
	 */
	private static boolean existsIn(Service service, LinkedList<Service> serviceList){
		for(Service currentService : serviceList){
			if(service.compareTo(currentService) == 0)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#runWorkload(boolean)
	 */
	@Override
	public void runWorkload(boolean seeOutput) {
		isRunning = true;
		soapTester.execute();
		isRunning = false;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#executedInvocations()
	 */
	@Override
	public LinkedList<Invocation> executedInvocations() {
		LinkedList<Invocation> invList = new LinkedList<Invocation>();
		for(HashMap<String, String> invMap : soapTester.listInvocations()){
			invList.add(new Invocation(getServiceByName(invMap.get(SERVICE_NAME_TAG), usedServices()), invMap.get(METHOD_NAME_TAG), invMap.get(START_TIME_TAG), invMap.get(END_TIME_TAG), invMap.get(RESPONSE_CODE)));
		}
		return invList;
	}
	
	/**
	 * Gets the service by name.
	 *
	 * @param serviceName the service name
	 * @param servicesList the services list
	 * @return the service by name
	 */
	public static Service getServiceByName(String serviceName, LinkedList<Service> servicesList){
		for(Service service : servicesList){
			if(service.getName().equals(serviceName))
				return service;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.monitor.master.workload.Workload#flush()
	 */
	@Override
	public void flush() {
		preferences = null;
		soapTester = null;
	}

}
