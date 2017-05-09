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
public class PrimitiveBeanAttribute extends BeanAttribute {

	public PrimitiveBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo) {
		super(objInstance, beanInfo, attInfo);
	}

	@Override
	public String getStringValue(Object attObject) {
		return BeanAttribute.decodePrimitiveType(attObject);
	}

	@Override
	public String getAttName() {
		return getBaseAttName();
	}

}
