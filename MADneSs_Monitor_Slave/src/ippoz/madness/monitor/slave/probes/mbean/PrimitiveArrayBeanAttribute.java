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
public class PrimitiveArrayBeanAttribute extends BeanAttribute {
	
	private int arrayIndex;

	public PrimitiveArrayBeanAttribute(ObjectInstance objInstance, MBeanInfo beanInfo, MBeanAttributeInfo attInfo, int arrayIndex) {
		super(objInstance, beanInfo, attInfo);
		this.arrayIndex = arrayIndex;
	}

	@Override
	public String getStringValue(Object attObject) {
		Object[] vect = (Object[]) attObject;
		for(int i=0;i<vect.length;i++){
			if(i == arrayIndex)
				return String.valueOf(vect[i]);
		}
		return null;
	}

	@Override
	public String getAttName() {
		return getBaseAttName() + "#" + arrayIndex;
	}

}
