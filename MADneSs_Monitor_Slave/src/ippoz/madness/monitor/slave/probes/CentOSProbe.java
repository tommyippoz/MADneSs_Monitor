/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.madness.commons.support.AppUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author root
 *
 */
public class CentOSProbe extends CycleProbe {

	private HashMap<String, String> involvedAttributes;
	
	public CentOSProbe(HashMap<String, String> involvedAttributes, String receiverIp, int probePort) {
		super(LayerType.CENTOS, "CentOS", receiverIp, probePort);
		this.involvedAttributes = involvedAttributes;
	}

	@Override
	protected HashMap<String, String> readParams() {
		HashMap<String, String> outMap = new HashMap<String, String>();
		outMap.putAll(getMemInfo());
		outMap.putAll(getVirtMemInfo());
		outMap.putAll(getCpuInfo());
		return outMap;
	}

	private HashMap<String, String> getMemInfo() {
		String[] splitted;
		String attValue;
		HashMap<String, String> outMap = new HashMap<String, String>();
		try {
			for(String readedRow : AppUtility.runScriptInto("cat /proc/meminfo", "", false)){
				splitted = readedRow.trim().split(":");
				if(involvedAttributes.containsKey(splitted[0].trim().toUpperCase())){
					if(splitted[1].trim().toUpperCase().endsWith("GB")){
						attValue = String.valueOf((int)(Double.parseDouble(splitted[1].trim().substring(0, splitted[1].trim().indexOf(" ")))*1000));
					} else if(splitted[1].trim().toUpperCase().endsWith("KB")){
						attValue = splitted[1].trim().substring(0, splitted[1].trim().indexOf(" "));
					}
					else attValue = readedRow.split(":")[1].trim();
					outMap.put(involvedAttributes.get(splitted[0].trim().toUpperCase()), attValue);
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read /proc/meminfo");
		}
		return outMap;
	}

	private HashMap<String, String> getVirtMemInfo() {
		String[] splitted;
		HashMap<String, String> outMap = new HashMap<String, String>();
		try {
			for(String readedRow : AppUtility.runScriptInto("cat /proc/vmstat", "", false)){
				splitted = readedRow.trim().split(" ");
				if(involvedAttributes.containsKey(splitted[0].trim().toUpperCase())){
					outMap.put(involvedAttributes.get(splitted[0].trim().toUpperCase()), splitted[1].trim());
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read /proc/meminfo");
		}
		return outMap;
	}
	
	private HashMap<String, String> getCpuInfo() {
		String[] splitted;
		HashMap<String, String> outMap = new HashMap<String, String>();
		LinkedList<String> outList;
		try {
			outList = AppUtility.runScriptInto("cat /proc/stat", "", false);
			if(outList.size() > 0){
				splitted = outList.getFirst().trim().split(" ");
				outMap.put("CPU User Processes", splitted[2].trim());
				outMap.put("CPU Niced Processes", splitted[3].trim());
				outMap.put("CPU Kernel Processes", splitted[4].trim());
				outMap.put("CPU Idle Processes", splitted[5].trim());
				outMap.put("CPU I/O Wait Processes", splitted[6].trim());
				outMap.put("CPU Interrupts", splitted[7].trim());
				outMap.put("CPU Soft Interrupts", splitted[8].trim());
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read /proc/stat");
		}
		return outMap;
	}

	@Override
	public void setupParameters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRun() {
		return AppUtility.isUNIX();
	}
	
}
