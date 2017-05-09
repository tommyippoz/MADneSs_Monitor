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
public class CompositeBeanAttribute extends BeanAttribute {
	
	private String compositeKey;

	public CompositeBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo, String compositeKey) {
		super(objInstance, beanInfo, attInfo);
		this.compositeKey = compositeKey;
	}

	@Override
	public String getStringValue(Object attObject) {
		if(attObject instanceof CompositeDataSupport)
			return ((CompositeDataSupport)attObject).get(compositeKey).toString();
		else return ((CompositeData)attObject).get(compositeKey).toString();
	}

	@Override
	public String getAttName() {
		return getBaseAttName() + "." + compositeKey;
	}

}
