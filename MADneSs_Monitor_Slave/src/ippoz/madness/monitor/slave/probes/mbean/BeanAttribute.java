/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import ippoz.madness.commons.support.AppLogger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;

import net.sf.json.JSONObject;

/**
 * @author Tommy
 *
 */
public abstract class BeanAttribute {
	
	private ObjectInstance objInstance;
	private MBeanInfo beanInfo;
	private MBeanAttributeInfo attInfo;
	
	public BeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo) {
		this.objInstance = objInstance;
		this.beanInfo = beanInfo;
		this.attInfo = attInfo;
	}
	
	public String getAttValue(MBeanServer server) {
		try {
			return getStringValue(server.getAttribute(objInstance.getObjectName(), attInfo.getName()));
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to get value");
			return null;
		}
	} 
	
	public JSONObject getJSONValue(MBeanServer server) {
		JSONObject json = new JSONObject();
		String value = getAttValue(server);
		json.put("beanName", getBeanName());
		json.put("attributeName", getBeanDetails());
		json.put("attributeValue", value);
		return json;
	}
	
	protected String getBaseAttName(){
		return objInstance.getClassName().substring(objInstance.getClassName().lastIndexOf(".")+1) + "." + attInfo.getName();
	}
	
	public String getBeanName(){
		return getAttName().substring(0, getAttName().indexOf("."));
	}
	
	public String getBeanDetails(){
		return getAttName().substring(getAttName().indexOf(".")+1);
	}
	
	public abstract String getStringValue(Object attObject);
	
	public abstract String getAttName();
	
	public ObjectInstance getBeanInstance() {
		return objInstance;
	}

	public MBeanInfo getBeanInfo() {
		return beanInfo;
	}

	public MBeanAttributeInfo getAttInfo() {
		return attInfo;
	}
	
	public static String decodePrimitiveType(Object attObject){
		String castedValue = null;
		if(attObject != null) {
			if(attObject.getClass().getName().equals("int") || attObject.getClass().getName().equals("java.lang.Integer"))
				castedValue = String.valueOf((Integer)attObject);
			else if(attObject.getClass().getName().equals("boolean") || attObject.getClass().getName().equals("java.lang.Boolean"))
				castedValue = String.valueOf((Boolean)attObject);
			else if(attObject.getClass().getName().equals("double") || attObject.getClass().getName().equals("java.lang.Double"))
				castedValue = String.valueOf((Double)attObject);
			else if(attObject.getClass().getName().equals("long") || attObject.getClass().getName().equals("java.lang.Long"))
				castedValue = String.valueOf((Long)attObject);
			else if(attObject.getClass().getName().equals("string") || attObject.getClass().getName().equals("java.lang.String"))
				castedValue = String.valueOf(attObject);
			else AppLogger.logException(BeanAttribute.class, new RuntimeException("Unable to cast " + attObject.getClass().getName() + " to a primitive type"), "Cast Error"); 
		}
		return castedValue;
	}

}
