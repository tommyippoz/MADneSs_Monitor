/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;

/**
 * @author Tommy
 *
 */
public class CompositeArrayBeanAttribute extends BeanAttribute {
	
	private int arrayIndex;
	private String key;

	public CompositeArrayBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo, int index, String key) {
		super(objInstance, beanInfo, attInfo);
		this.arrayIndex = index;
		this.key = key;
	}

	@Override
	public String getStringValue(Object attObject) {
		Object[] vect = (Object[]) attObject;
		if(vect[arrayIndex] instanceof CompositeDataSupport)
			return ((CompositeDataSupport)vect[arrayIndex]).get(key).toString();
		else return ((CompositeData)vect[arrayIndex]).get(key).toString();
	}

	@Override
	public String getAttName() {
		return getBaseAttName() + "." + key + "#" + arrayIndex;
	}

}
