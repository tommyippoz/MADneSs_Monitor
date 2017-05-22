/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The Class JMXProbe. Gathers data from the JVM
 *
 * @author Tommy
 */
public class JMXProbe extends ScriptProbe {

	/**
	 * Instantiates a new JMX probe.
	 *
	 * @param filePath the file path
	 * @param fileArgs the file arguments
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 */
	public JMXProbe(String filePath, String fileArgs, String receiverIp, int probePort) {
		super(System.getProperty("user.dir") + "/" + filePath, fileArgs, LayerType.JVM, "JMX", receiverIp, probePort);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#setupParameters()
	 */
	@Override
	public void setupParameters() {
		File preferencesFile;
		BufferedReader reader;
		BufferedWriter writer;
		String toWrite = "";
		String readed;
		try {
			new File(getFilePath());
			preferencesFile = new File((new File(getFilePath())).getAbsolutePath().replaceAll((new File(getFilePath())).getName(), "") + "beanFetcher.preferences");
			if(preferencesFile.exists()){
				
				reader = new BufferedReader(new FileReader(preferencesFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed.contains("dest_ip_address"))
						toWrite = toWrite + "dest_ip_address=" + getReceiverIp();
					else if(readed.contains("dest_port_number"))
						toWrite = toWrite + "dest_port_number=" + getProbePort();
					else toWrite = toWrite + readed;
					toWrite = toWrite + "\n"; 
				}
				reader.close();
				
				preferencesFile.delete();
				writer = new BufferedWriter(new FileWriter(preferencesFile));
				writer.write(toWrite);
				writer.close();
				
			} else AppLogger.logError(getClass(), "NoSuchJMXProbePreferencesFile", "unable to find " + preferencesFile.getAbsolutePath());
		} catch(FileNotFoundException ex){
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#canRun()
	 */
	@Override
	public boolean canRun() {
		return true;
	}

}