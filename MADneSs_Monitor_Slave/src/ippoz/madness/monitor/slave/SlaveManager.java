/**
 * 
 */
package ippoz.madness.monitor.slave;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;
import ippoz.madness.commons.support.PreferencesManager;
import ippoz.madness.monitor.communication.CommunicationManager;
import ippoz.madness.monitor.communication.MessageType;
import ippoz.madness.monitor.slave.probes.CentOSProbe;
import ippoz.madness.monitor.slave.probes.JMXLocalProbe;
import ippoz.madness.monitor.slave.probes.Probe;
import ippoz.madness.monitor.slave.probes.ProbeManager;
import ippoz.madness.monitor.slave.probes.UnixNetworkProbe;
import ippoz.madness.monitor.slave.sut.JSeduite;
import ippoz.madness.monitor.slave.sut.Liferay612SUT;
import ippoz.madness.monitor.slave.sut.SUT;
import ippoz.madness.monitor.slave.sut.injection.BashInjection;
import ippoz.madness.monitor.slave.sut.injection.EnvInjection;
import ippoz.madness.monitor.slave.sut.injection.Injection;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class SlaveManager {
	
	private static final String JVM_PROBE = "JVM_ATTRIBUTES_FILE";
	
	private static final String CENTOS_PROBE = "OS_ATTRIBUTES_FILE";
	
	private static final String ANOMALY_ACTIVATION_FILE = "ANOMALY_ACTIVATION_FILE";
	
	private ProbeManager pManager;
	private CommunicationManager cManager;
	private Thread listenerThread;
	private SUT systemUnderTest;
	private PreferencesManager prefManager;
	
	public SlaveManager(File prefFile) throws IOException {
		prefManager = new PreferencesManager(prefFile);
		pManager = new ProbeManager();
		cManager = new CommunicationManager(prefManager.getPreference("MASTER_IP_ADDRESS"), Integer.parseInt(prefManager.getPreference("OUT_PORT")), Integer.parseInt(prefManager.getPreference("IN_PORT")));
	}
	
	public void startListener(){
		listenerThread = new Thread(new SlaveListener());
		AppLogger.logInfo(getClass(), "Listener '" + AppUtility.getIP().getHostAddress().toString() + "' waiting for master at '" + prefManager.getPreference("MASTER_IP_ADDRESS") + "'");
		listenerThread.start();
	}
	
	public void flush() throws IOException {
		pManager.shutdownProbes();
		if(!listenerThread.isInterrupted())
			listenerThread.interrupt();
		cManager.flush();
	}
	
	private void takeAction(Collection<Object> receivedCommands) throws IOException, InterruptedException {
		String toCompare;
		Object[] commArray = receivedCommands.toArray();
		if(receivedCommands.size() == 0)
			AppLogger.logError(getClass(), "Unrecognized message", "No messages received");
		else if(!(commArray[0] instanceof MessageType))
			AppLogger.logError(getClass(), "Unrecognized message", "Malformed message");
		else {
			AppLogger.logInfo(getClass(), "Received: " + ((MessageType)(commArray[0])).toString());
			switch((MessageType)(commArray[0])){
				case PING:
					cManager.send(MessageType.OK);
					break;
				case ADD_PROBE:
					Probe newProbe = null;
					PreferencesManager probePrefManager = new PreferencesManager(new File(prefManager.getPreference("PROBE_PREFERENCES_FILE")));
					switch(((LayerType)commArray[1])){
						case JVM:
							if(probePrefManager.getPreference(JVM_PROBE) != null && new File(probePrefManager.getPreference(JVM_PROBE)).exists()){
								newProbe = new JMXLocalProbe(cManager.getIpAddress(), (Integer)commArray[2], new File(probePrefManager.getPreference(JVM_PROBE)));
							} else AppLogger.logError(getClass(), "ProbeError", "Unable to instantiate JVM probe: preferences are not valid");
							break;
						case CENTOS:
							newProbe = new CentOSProbe(AppUtility.readPairsFromCSV(probePrefManager.getPreference(CENTOS_PROBE)), cManager.getIpAddress(), (Integer)commArray[2]);
							break;
						case UNIX_NETWORK:
							newProbe = new UnixNetworkProbe(cManager.getIpAddress(), (Integer)commArray[2]);
							break;
						default:
							AppLogger.logError(getClass(), "ProbeNotRecognized", "Unable to recognize probe type");
							break;
					}
					if(newProbe != null) {
						pManager.addProbe(newProbe);
					}
					cManager.send(MessageType.OK);
					break;
				case SETUP_SUT:
					SUT sut = null;
					toCompare = ((String)commArray[1]).toUpperCase();
					if(toCompare.equals("LIFERAYSUT")){
						sut = new Liferay612SUT("Liferay 6.1.2 CE SUT", new File(prefManager.getPreference("SUT_PREFERENCES_FILE")));
					} else if(toCompare.equals("JSEDUITESUT")){
						sut = new JSeduite("jSeduite - GlassFish V2.1", new File(prefManager.getPreference("SUT_PREFERENCES_FILE")));
					} else {
						AppLogger.logError(getClass(), "SUTNotRecognized", "Unable to recognize SUT type");
					}
					systemUnderTest = sut;
					cManager.send(MessageType.OK);
					break;
				case START_EXPERIMENT:
					systemUnderTest.setInjections(parseInjections(commArray));
					systemUnderTest.startSUT();
					pManager.startProbes();
					cManager.send(MessageType.OK);
					break;
				case END_EXPERIMENT:
					pManager.shutdownProbes();
					cManager.send(MessageType.CHECK_PROBE);
					systemUnderTest.shutdownSUT();
					cManager.send(buildInjActivations());
					break;
				case START_CAMPAIGN:
					cManager.send(new Object[]{MessageType.OK, System.currentTimeMillis()});
					break;
				case END_CAMPAIGN:
					cManager.send(MessageType.OK);
					flush();
					break;
				default:
					System.out.println("UNRECOGNIZED COMMAND");
					break;
			
			}
		}
	}
	
	private Object[] buildInjActivations() {
		String[] actArray;
		if(systemUnderTest.hasInjections())
			actArray = systemUnderTest.getInjectionTimestamps();
		else actArray = new String[0];
		Object[] outArray = new Object[actArray.length + 1];
		for(int i=0;i<actArray.length;i++){
			outArray[i+1] = actArray[i];
		}
		outArray[0] = MessageType.OK;
		return outArray;
	}

	private LinkedList<Injection> parseInjections(Object[] commArray) {
		Injection current = null;
		String toCompare;
		LinkedList<Injection> outList;
		if(commArray.length > 1){
			outList = new LinkedList<Injection>();
			for(int i=1;i<commArray.length;i++){
				if(commArray[i] instanceof String){
					toCompare = ((String) commArray[i]).split(";")[0].toUpperCase();
					if(toCompare.equals("TEST")){
						current = new BashInjection("FIREFOX", Long.valueOf(((String) commArray[i]).split(";")[2]), "firefox -search pokemon", "pkill -f firefox");
					} if(systemUnderTest.hasInjection(toCompare)){
							current = new EnvInjection(toCompare, Long.valueOf(((String) commArray[i]).split(";")[2]), systemUnderTest.getStartScriptPath(), prefManager.getPreference(ANOMALY_ACTIVATION_FILE), parseInjDetails(toCompare, ((String) commArray[i]).split(";")));
					} else {
						AppLogger.logError(getClass(), "Unrecognized" + toCompare + "injection", "Unable to recognize test type " + ((String) commArray[i]).split(",")[1]);
					}
					outList.add(current);
				}
			}
			return outList;
		} else return null;
	}
	
	private String[] parseInjDetails(String injTag, String[] details){
		String[] outList = new String[(int)(details.length/3)+1];
		for(int i=1;i<details.length;i=i+3){
			outList[(i-1)/3] = details[i];
		}
		outList[outList.length-1] = "FAILURE_" + injTag;
		return outList;
	}

	private class SlaveListener implements Runnable {
	
		@Override
		public void run() {
			Collection<Object> objColl;
			while(cManager.isAlive()){
				try {
					objColl = cManager.receive();
					if(objColl != null)
						takeAction(objColl);
					else break;
				} catch (IOException ex) {
					AppLogger.logException(getClass(), ex, "Unable to receive data");
					break;
				} catch (InterruptedException ex) {
					AppLogger.logException(getClass(), ex, "Error while executing");
					break;
				}
			}
		}
		
	}

}
