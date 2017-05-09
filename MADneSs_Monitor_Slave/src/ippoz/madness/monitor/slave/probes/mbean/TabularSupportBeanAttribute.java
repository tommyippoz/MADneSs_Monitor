/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;

/**
 * @author Tommy
 *
 */
public class TabularSupportBeanAttribute extends BeanAttribute {
	
	private String key;

	public TabularSupportBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo, String key) {
		super(objInstance, beanInfo, attInfo);
		this.key = key;
	}

	@Override
	public String getStringValue(Object attObject) {
		return ((CompositeDataSupport)((TabularDataSupport)attObject).get(new String[]{key})).get("value").toString();
	}

	@Override
	public String getAttName() {
		return getBaseAttName() + ".\"" + key + "\"";
	}

}
