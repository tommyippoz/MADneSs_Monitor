/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author Tommy
 *
 */
public class ObjectNameBeanAttribute extends BeanAttribute {
	
	private String propertyKey;

	public ObjectNameBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo, String propertyKey) {
		super(objInstance, beanInfo, attInfo);
		this.propertyKey = propertyKey;
	}

	@Override
	public String getStringValue(Object attObject) {
		return ((ObjectName)attObject).getKeyProperty(propertyKey);
	}

	@Override
	public String getAttName() {
		return getBaseAttName() + "." + propertyKey;
	}

}
