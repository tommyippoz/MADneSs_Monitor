/**
 * 
 */
package ippoz.madness.monitor.slave;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import ippoz.madness.monitor.slave.probes.mbean.BeanManager;

/**
 * @author andrea
 *
 */
public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BeanManager bM = new BeanManager();
			bM.loadBeanPreferences(new File("datafiles/slave/probes/JMX/bean.preferences"));
			bM.writeBeansToFile("./AB.csv");
			HashMap<String, String> val = bM.getObsValues(); 
			for(String key : val.keySet()){
				System.out.println(key + " | " + val.get(key));
			}
		
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReflectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
