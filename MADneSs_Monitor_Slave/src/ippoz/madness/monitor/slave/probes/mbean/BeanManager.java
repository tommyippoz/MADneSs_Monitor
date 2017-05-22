/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import ippoz.madness.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Tommy
 *
 */
public class BeanManager {
	
	private MBeanServer beanServer;
	private LinkedList<BeanAttribute> standardAttributes;
	private LinkedList<BeanAttribute> selectedAttributes;
	
	public BeanManager() throws IntrospectionException, InstanceNotFoundException, ReflectionException{
		beanServer = ManagementFactory.getPlatformMBeanServer();
		loadDefaultBeans();
	}
	
	private void loadDefaultBeans() throws IntrospectionException, InstanceNotFoundException, ReflectionException {
		Set<ObjectInstance> instances = null;
		Iterator<ObjectInstance> iterator = null;
		MBeanInfo mbeanInfo = null;
		Object attValue = null;
		instances = beanServer.queryMBeans(null, null);
		iterator = instances.iterator();
		standardAttributes = new LinkedList<BeanAttribute>();
		while (iterator.hasNext()) {
			ObjectInstance instance = iterator.next();
			mbeanInfo = beanServer.getMBeanInfo(instance.getObjectName());
			for(MBeanAttributeInfo att : mbeanInfo.getAttributes()){
				try {
					attValue = beanServer.getAttribute(instance.getObjectName(), att.getName());
					analyzeBeanAttribute(instance, mbeanInfo, att, attValue);
				} catch (Exception ex) {
					//AppLogger.logException(getClass(), ex, "Unable to get default Attribute from Beans");
				}
			}
		}
		AppLogger.logInfo(getClass(), "Available Beans: " + standardAttributes.size());
	}

	@SuppressWarnings("unchecked")
	private void analyzeBeanAttribute(ObjectInstance objInstance, MBeanInfo mBeanInfo, MBeanAttributeInfo att, Object attValue) {
		if(attValue != null){
			if(attValue.getClass().getName().equals("int") || attValue.getClass().getName().equals("java.lang.Integer")
					 || attValue.getClass().getName().equals("boolean") || attValue.getClass().getName().equals("java.lang.Boolean")
					 || attValue.getClass().getName().equals("double") || attValue.getClass().getName().equals("java.lang.Double")
					 || attValue.getClass().getName().equals("long") || attValue.getClass().getName().equals("java.lang.Long")
					 || attValue.getClass().getName().equals("string") || attValue.getClass().getName().equals("java.lang.String"))
				standardAttributes.add(new PrimitiveBeanAttribute(objInstance, mBeanInfo, att));
			else if(attValue.getClass().getName().equals("[Lint[L") || attValue.getClass().getName().equals("[Ljava.lang.Integer;")
					 || attValue.getClass().getName().equals("[Lboolean;") || attValue.getClass().getName().equals("[Ljava.lang.Boolean;")
					 || attValue.getClass().getName().equals("[Ldouble;") || attValue.getClass().getName().equals("[Ljava.lang.Double;")
					 || attValue.getClass().getName().equals("[Llong;") || attValue.getClass().getName().equals("[Ljava.lang.Long;")
					 || attValue.getClass().getName().equals("[Lstring;") || attValue.getClass().getName().equals("[Ljava.lang.String;")){
				Object[] vect = (Object[]) attValue;
				for(int i=0;i<vect.length;i++){
					standardAttributes.add(new PrimitiveArrayBeanAttribute(objInstance, mBeanInfo, att, i));
				}
			} else if(attValue.getClass().getName().equals("[J"))
				standardAttributes.add(new ArrayBeanAttribute(objInstance, mBeanInfo, att));
			else if(attValue.getClass().getName().equals("javax.management.ObjectName")){
				for(String key : ((ObjectName)attValue).getKeyPropertyList().keySet()) {
					standardAttributes.add(new ObjectNameBeanAttribute(objInstance, mBeanInfo, att, key));
				}
			} else if(attValue.getClass().getName().equals("javax.management.openmbean.CompositeDataSupport")) {
				Iterator<?> cdsIt = ((CompositeDataSupport)attValue).getCompositeType().keySet().iterator();
				while(cdsIt.hasNext()){
					standardAttributes.add(new CompositeBeanAttribute(objInstance, mBeanInfo, att, cdsIt.next().toString()));
				}
			} else if(attValue.getClass().getName().equals("[Ljavax.management.openmbean.CompositeData;")){
				CompositeData[] cdVect = (CompositeData[]) attValue;
				for(int i=0;i<cdVect.length;i++){
					for(String key : cdVect[i].getCompositeType().keySet()) {
						standardAttributes.add(new CompositeArrayBeanAttribute(objInstance, mBeanInfo, att, i, key));
					}
				}
			} else if(attValue.getClass().getName().equals("javax.management.openmbean.TabularDataSupport")) {
				for(Object key : ((TabularDataSupport)attValue).keySet()){
					standardAttributes.add(new TabularSupportBeanAttribute(objInstance, mBeanInfo, att, ((List<String>)key).get(0)));
				}
			} else AppLogger.logException(getClass(), new RuntimeException("Unable to process " + attValue.getClass().getName() + " type"), "Cast Error");	
		}
	}

	public void writeBeansToFile(String filename) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
		writer.write("bean_name;bean_details\n");
		for(BeanAttribute bAtt : standardAttributes){
			writer.write(bAtt.getBeanName() + ";" + bAtt.getBeanDetails() + "\n");
		}
		writer.close();
	}
	
	public void loadBeanPreferences(File beanPrefFile) {
		String readed = null;
		BufferedReader reader = null;
		selectedAttributes = new LinkedList<BeanAttribute>();
		try {
			reader = new BufferedReader(new FileReader(beanPrefFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed.length() > 0){
					for(BeanAttribute bAtt : standardAttributes){
						if(bAtt.getAttName().equals(readed.trim())) {
							selectedAttributes.add(bAtt);
							break;
						}
					}
				}
			}
			reader.close();
			standardAttributes.clear();
			AppLogger.logInfo(getClass(), "Loaded Beans: " + selectedAttributes.size());
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to load Bean Preferences");
		}
	}
	
	public void loadBeanPreferences(String preferencesFilename) {
		loadBeanPreferences(new File(preferencesFilename));
	}

	public JSONObject getObservation(){
		JSONObject jObj = new JSONObject();
		JSONArray jArr = new JSONArray();
		long startTime = System.currentTimeMillis();
		jObj.put("time_ms", System.currentTimeMillis());
		jObj.put("datetime", new Date().toString());
		for(BeanAttribute bAtt : selectedAttributes){
			jArr.add(bAtt.getJSONValue(beanServer));
		}
		jObj.put("processingTime_ms", (System.currentTimeMillis() - startTime));
		jObj.put("observations", jArr);
		return jObj;
		
	}
	
	public HashMap<String, String> getObsValues(){
		HashMap<String, String> outMap = new HashMap<String, String>();
		for(BeanAttribute bAtt : selectedAttributes){
			outMap.put(bAtt.getAttName(), bAtt.getAttValue(beanServer));
		}
		return outMap;
	}
	
}
