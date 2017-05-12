/**
 * 
 */
package ippoz.madness.commons.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class AppUtility {

	public static boolean isWindows(){
		return System.getProperty("os.name").toUpperCase().contains("WIN");
	}
	
	public static boolean isUNIX(){
		return System.getProperty("os.name").toUpperCase().contains("UNIX");
	}
	
	public static LinkedList<String> runScriptInto(String path, String args, boolean setOnFolder) throws IOException {
		Process p;
		LinkedList<String> outList = new LinkedList<String>();
		BufferedReader reader = null;
		String script = buildScript(path);
		if(setOnFolder)
			p = Runtime.getRuntime().exec(script + " " + args, null, new File((new File(path)).getAbsolutePath().replaceAll((new File(path)).getName(), "")));
		else p = Runtime.getRuntime().exec(script + " " + args);
		String readed;
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((readed = reader.readLine()) != null) {
            outList.add(readed);
        }
        reader.close();
		//AppLogger.logInfo(Probe.class, "Executed \"" + script + "\"");
		return outList;
	}
	
	public static Process runScript(String path, String args, boolean setOnFolder, boolean viewOutput, boolean waitFor) throws IOException{
		Process p;
		BufferedReader reader = null;
		String script = buildScript(path);
		if(setOnFolder)
			p = Runtime.getRuntime().exec(script + " " + args, null, new File((new File(path)).getAbsolutePath().replaceAll((new File(path)).getName(), "")));
		else if(args != null)
			p = Runtime.getRuntime().exec(script + " " + args);
		else p = Runtime.getRuntime().exec(script);
		if(waitFor){
			String readed;
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while ((readed = reader.readLine()) != null) {
	            if(viewOutput)
	            	System.out.println(readed);
	        }
	        reader.close();
        }
		AppLogger.logInfo(AppUtility.class, "Executed \"" + script + "\"");
		return p;
	}
	
	public static HashMap<String, String> readPairsFromCSV(String filename) {
		HashMap<String, String> pairMap = new HashMap<String, String>();
		BufferedReader reader;
		String readed;
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
			while(reader.ready()){
				readed = reader.readLine().trim();
				if(readed.length() > 0 && readed.contains(",")){
					pairMap.put(readed.split(",")[0].trim().toUpperCase(), readed.split(",")[1].trim());
				}
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(AppUtility.class, ex, "Unable to read pair CSV: '" + filename + "'");
		}
		return pairMap;
	}

	public static long parseStringTime(String stringData, int hourOffset) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(format.parse(stringData));
			cal.add(Calendar.HOUR, hourOffset);
			return cal.getTimeInMillis();
		} catch (ParseException ex) {
			AppLogger.logException(AppUtility.class, ex, "Unable to convert data");
		}
		return 0;
	}
	
	public static Process runScript(String path, String args, boolean setOnFolder, boolean viewOutput) throws IOException{
		Process p;
		BufferedReader reader = null;
		String script = buildScript(path);
		if(setOnFolder)
			p = Runtime.getRuntime().exec(script + " " + args, null, new File((new File(path)).getAbsolutePath().replaceAll((new File(path)).getName(), "")));
		else p = Runtime.getRuntime().exec(script + " " + args);
		if(viewOutput){
			reader = new BufferedReader(new InputStreamReader(
	        p.getInputStream()));
	        while (reader.ready()) {
	            System.out.println(reader.readLine());
	        }
	        reader.close();
        }
		//AppLogger.logInfo(Probe.class, "Executed \"" + script + "\"");
		return p;
	}
	
	private static String buildScript(String path){
		String script = path;
		if(path.endsWith(".jar"))
			script = "java -jar " + path;
		return script;
	}
	
	public static HashMap<String, String> loadPreferences(File prefFile, String[] tags) throws IOException {
		String readed, tag, value;
		BufferedReader reader;
		HashMap<String, String> map = new HashMap<String, String>();
		if(prefFile.exists()){
			reader = new BufferedReader(new FileReader(prefFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed.length() > 0) {
					if(readed.contains("=") && readed.split("=").length == 2){
						tag = readed.split("=")[0];
						value = readed.split("=")[1];
						if(tags != null && tags.length > 0){
							for(String current : tags){
								if(current.toUpperCase().equals(tag.toUpperCase())){
									map.put(tag.trim(), value.trim());
									break;
								}
							}
						} else map.put(tag.trim(), value.trim());
					}
				}
			}
			reader.close();
		} else {
			AppLogger.logInfo(AppUtility.class, "Unexisting preference file: " + prefFile.getAbsolutePath());
		}
		return map;
	}
	
	public static String formatMillis(long dateMillis){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(dateMillis));
	}
	
	public static Date getDateFromString(String dateString){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			AppLogger.logException(AppUtility.class, e, "Unable to parse date '" + dateString + "'");
			return null;
		}
	}
	
	public static Double calcAvg(Double[] values){
		double mean = 0;
		for(Double d : values){
			mean = mean + d;
		}
		return mean / values.length;
	}
	
	public static Double calcMedian(Double[] values){
		Arrays.sort(values);
		return values[(int)(values.length/2)];
	}
	
	public static Double calcMode(Double[] values){
		int freq = 0, modeFreq = 0;
		double mode = 0;
		Arrays.sort(values);
		for(int i=0;i<values.length;i++){
			if(i > 0){
				if(values[i] == values[i-1])
					freq++;
				else {
					if(freq >= modeFreq){
						mode = values[i-1];
						modeFreq = freq;
						freq = 1;
					}
				}
			} else freq++;
		}
		return mode;
	}
	
	public static Double calcStd(Double[] values, Double mean){
		double std = 0;
		for(Double d : values){
			std = std + Math.pow(d-mean, 2);
		}
		return std / values.length;
	}
	
	public static InetAddress getIP(){
		try {
	        InetAddress candidateAddress = null;
	        // Iterate all NICs (network interface cards)...
	        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // Iterate all IP addresses assigned to each card...
	            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {

	                    if (inetAddr.isSiteLocalAddress()) {
	                        // Found non-loopback site-local address. Return it immediately...
	                        return inetAddr;
	                    }
	                    else if (candidateAddress == null) {
	                        // Found non-loopback address, but not necessarily site-local.
	                        // Store it as a candidate to be returned if site-local address is not subsequently found...
	                        candidateAddress = inetAddr;
	                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
	                        // only the first. For subsequent iterations, candidate will be non-null.
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            // We did not find a site-local address, but we found some other non-loopback address.
	            // Server might have a non-site-local address assigned to its NIC (or it might be running
	            // IPv6 which deprecates the "site-local" concept).
	            // Return this non-loopback candidate address...
	            return candidateAddress;
	        }
	        // At this point, we did not find a non-loopback address.
	        // Fall back to returning whatever InetAddress.getLocalHost() returns...
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        if (jdkSuppliedAddress == null) {
	            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
	        }
	        return jdkSuppliedAddress;
	    }
	    catch (Exception e) {
	        AppLogger.logError(AppUtility.class, "NetworkError", "Failed to determine LAN address: " + e);
	    }
		return null;
	}
	
}
