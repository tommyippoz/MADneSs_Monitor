/**
 * 
 */
package ippoz.madness.monitor.slave.probes.mbean;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;

/**
 * @author Tommy
 *
 */
public class ArrayBeanAttribute extends BeanAttribute {

	public ArrayBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo) {
		super(objInstance, beanInfo, attInfo);
	}

	@Override
	public String getStringValue(Object attObject) {
		String result = "";
		for(Object current : (long[]) attObject){
			result = result + current + " ";
		}
		return result.substring(0, result.length()-1);
	}

	@Override
	public String getAttName() {
		return getBaseAttName();
	}

}
