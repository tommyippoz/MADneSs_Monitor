/**
 * 
 */
package ippoz.multilayer.monitor.master.workload;

import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.PreferencesManager;
import ippoz.multilayer.monitor.master.services.Invocation;
import ippoz.multilayer.monitor.master.services.JSeduiteService;
import ippoz.multilayer.monitor.master.services.Service;

import org.apache.commons.validator.UrlValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import pt.uc.dei.wsrbench.common.configuration.Configuration;
import pt.uc.dei.wsrbench.common.exception.DriverException;
import pt.uc.dei.wsrbench.common.pojo.Wsdl;
import pt.uc.dei.wsrbench.core.AsynchDriverImpl;
import pt.uc.dei.wsrbench.core.SynchDriverImpl;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author ippoz
 *
 */
public class JSeduiteWorkload extends Workload {
	
	private static final String W_MIN = "MIN_EXE_TIME";
	private static final String W_MAX = "MAX_EXE_TIME";
	private static final String W_DELAY = "SERVICES_DELAY_MS";
	private static final String W_LIST = "SERVICES_LIST";
	
	private int msDelay;
	private String ipAddress;
	private boolean isRunning;
	private LinkedList<Service> servicesList;
	private LinkedList<Invocation> invList;
	
	public JSeduiteWorkload(String name, PreferencesManager wPref, String serverIP) {
		this(name, wPref.getIntPreference(W_MIN), wPref.getIntPreference(W_MAX), serverIP, wPref.getIntPreference(W_DELAY), wPref.getPreference(W_LIST));
	}
	
	private JSeduiteWorkload(String name, int minTime, int maxTime, String ipAddress, int msDelay, String servList) {
		super(name, minTime, maxTime);
		isRunning = false;
		this.ipAddress = ipAddress;
		this.msDelay = msDelay;
		if(servList != null && servList.length() > 0){
			parseServList(servList);
		}
	}
	
	private JSeduiteWorkload(String name, int minTime, int maxTime, String ipAddress, int msDelay, LinkedList<Service> sList) {
		this(name, minTime, maxTime, ipAddress, msDelay, "");
		servicesList = sList;
	}
	
	private void parseServList(String servListString) {
		LinkedList<Service> allServices = getAllJSeduiteServices();
		servicesList = new LinkedList<Service>();
		for(String sName : parseServicesString(servListString)){
			if(existsIn(sName, allServices)){
				servicesList.add(getServiceFrom(sName, allServices));
			}
		}
	}
	
	private LinkedList<String> parseServicesString(String sString){
		LinkedList<String> ssList = new LinkedList<String>();
		for(String pString : sString.split(",")){
			ssList.add(pString.trim());
		}
		return ssList;
	}

	@Override
	public boolean isValid() {
		return servicesList != null && servicesList.size() > 0;
	}
	
	@Override
	public LinkedList<Service> usedServices() {
		LinkedList<Service> uniqueServices = new LinkedList<Service>();
		for(Service s : servicesList){
			if(!uniqueServices.contains(s)){
				uniqueServices.add(s);
			}
		}
		return uniqueServices;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	@Override
	public void runWorkload(boolean seeOutput) {
		long invStart;
		String callReply;
		JSeduiteInvoker jsInvoker;
		try {
			jsInvoker = new JSeduiteInvoker();
			isRunning = true;
			invList = new LinkedList<Invocation>();
			for(Service s : servicesList){
				invStart = System.currentTimeMillis();
				callReply = jsInvoker.invokeService((JSeduiteService)s, true);
				invList.add(new Invocation(s, s.getName(), invStart, System.currentTimeMillis(), callReply));	
				Thread.sleep(msDelay);
			}
			isRunning = false;
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Error while performing workload");
		}
	}
	
	@Override
	public LinkedList<Invocation> executedInvocations() {
		return invList;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Workload cloneWorkload() {
		return new JSeduiteWorkload(getName(), getMinExecutionTime(), getMaxExecutionTime(), ipAddress, msDelay, (LinkedList<Service>)servicesList.clone());
	}
	
	@Override
	public void flush() {
		servicesList.clear();
		servicesList = null;
	} 
	
	private static LinkedList<Service> getAllJSeduiteServices(){
		LinkedList<Service> sList = new LinkedList<Service>();
		sList.add(new JSeduiteService("PictureSetVirtual", true));
		sList.add(new JSeduiteService("PictureSet", true));
		sList.add(new JSeduiteService("PicasaWrapper", true));
		sList.add(new JSeduiteService("ApalWrapper", true));
		sList.add(new JSeduiteService("BreakingNews", true));
		sList.add(new JSeduiteService("FeedRegistry", true));
		sList.add(new JSeduiteService("FileUploader", true));
		sList.add(new JSeduiteService("FlickrWrapper", true));
		sList.add(new JSeduiteService("ICalReader", true));
		sList.add(new JSeduiteService("InternalNews", true));
		sList.add(new JSeduiteService("RssReader", true));
		return sList;
	}
	
	public class JSeduiteInvoker implements Validator {

		String NOTHING_TO_TEST="nothing to test";
		String ADDRESS_NOT_REACHABLE="Address not reachable";
		int ADDRESS_NOT_REACHABLE_int=0;
		String ERROR = "A fatal problem has ocurred during test execution";
		int ERROR_int=1;
		String FINISHED = "Finished";
		int FINISHED_int=2;
		boolean isValid=false;

		/**
		 * from Nuno code: detects if an address is valid
		 * (otherwise wsrbench does not work and stuck on invalid address)
		 * @param target
		 * @param errors
		 */
		@Override
		@SuppressWarnings("deprecation")
		public void validate(Object target, Errors errors)
		{
			Long timeout=Configuration.DEFAULT_TIMEOUT;

			Wsdl wsdl1 = (Wsdl) target;
			UrlValidator urlValidator = new UrlValidator();
			isValid = urlValidator.isValid(wsdl1.getUrl());
			System.out.println("try "+isValid);
			// check if server exists
			if (isValid)
			{
				HttpClient client = new HttpClient();
				client.setConnectionTimeout(timeout.intValue());
				HttpMethod method = new GetMethod(wsdl1.getUrl());

				try
				{
					int statusCode = client.executeMethod(method);
					if (statusCode != HttpStatus.SC_OK)
					{
						isValid = false;
						return;
					}
				} catch (HttpException e)
				{
					isValid = false;
					return;
					//e.printStackTrace();
				} catch (IOException e)
				{
					isValid = false;
					return;
					//e.printStackTrace();
				}
			}
			return;
		}

		/**
		 * (from Nuno stuff)
		 */
		@Override
		@SuppressWarnings("rawtypes") 
		public boolean supports(Class clazz)
		{
			boolean result = false;

			try {
				result = (clazz.equals(Wsdl.class));
			} catch (ClassCastException e) {
			}
			return result;
		}

	    public String prepareStringForWsrBench(String address){
			address=address+"?wsdl";
			return address;
		}

	    private String invokeService(JSeduiteService jss, boolean printOutput){
	    	String fullUrl = jss.generateCompleteURL(ipAddress);
	    	Wsdl wsdl2 = new Wsdl();
			wsdl2.setUrl(fullUrl);
			if(printOutput)
				System.out.println(wsdl2.toString());
			isValid = false;
			//set some config parameters
			Configuration config = new Configuration();
			if(printOutput)
				System.out.println("T_SERVICE: Set config information for current test");

			//validate url using stuff from Nuno: check if address is reachable
			validate(wsdl2, null);
	                
			if (isValid==false){
				wsdl2.setStatus(Wsdl.NOT_STARTED);
				System.out.println("T_SERVICE: test not started. Address not reachable: "+ ipAddress);
				return ADDRESS_NOT_REACHABLE;
			}
			if(printOutput)
				System.out.println(wsdl2.toString());

			//if url is valid, the test starts.
			//Set how many faulty requests should be sent for each injected fault.
			//MAX_FAULTY_REQUEST_COUNT=10;
			config.setFaultyRequestCount(Configuration.MAX_FAULTY_REQUEST_COUNT);
				//Number of requests without faults to send to each operation.
			//MAX_PLAIN_REQUEST_COUNT=10;
			config.setPlainRequestCount(Configuration.MAX_PLAIN_REQUEST_COUNT);

			//Define the timeout value for each connection to the server.
			//DEFAULT_TIMEOUT=10000L
			config.setTimeout(Configuration.DEFAULT_TIMEOUT);
			if(printOutput)
				System.out.println("T_SERVICE: WsrBench config information are now set and are:");
			if(printOutput)
				System.out.println("T_SERVICE: "+config.toString());
			if(printOutput)
				System.out.println(wsdl2.toString());

			SynchDriverImpl sinctest = new SynchDriverImpl();
			//get the wsdl and analyzes it
			try {
				wsdl2 = sinctest.submitWsdl(config, wsdl2);
			} catch (DriverException e) {
				System.out.println("T_SERVICE: impossible to retrieve the wsdl file.");
				System.out.println("T_SERVICE: test aborted with result "+ ERROR);
				return ERROR;
			}
			if(printOutput)
				System.out.println(wsdl2.toString());
			AsynchDriverImpl asinctest = new AsynchDriverImpl();
			//set wsdl and config files
			asinctest.setWsdl(wsdl2);
			asinctest.setConfig(config);
			//start the test
			asinctest.startWsdlTestA();
			//System.out.println(wsdl.toString());

			//get result of the test from pt.uc.dei.wsrbench.common.pojo.Wsdl.java
			String result=wsdl2.getStatus();
			//System.out.println(wsdl.toString());
			if(printOutput)
				System.out.println("T_SERVICE: test of "+ fullUrl + "completed with result: " + result);

			if(printOutput)
				System.out.println("PRINTING wsdl.getOperationList().toString()");
			if(printOutput)
				System.out.println(wsdl2.getOperationList().toString());
			if(printOutput)
				System.out.println("PRINTING size of operationList wsdl.getOperationList().size()");
			if(printOutput)
				System.out.println(wsdl2.getOperationList().size());
			if(printOutput)
				System.out.println("PRINTING element 0 of operationList wsdl.getOperationList().get(0)");
			if(printOutput)
				System.out.println(wsdl2.getOperationList().get(0));

			if(result.equals(FINISHED)){
				return result;
			} else {
				System.out.println("T_SERVICE: STRANGE STUFF HERE!Check carefully!");
				return result;
			}
	    }

	}

}
