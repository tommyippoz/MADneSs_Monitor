/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public abstract class RemoteService extends Service {

	private LinkedList<MethodCall> methodList;
	
	public RemoteService(String name) {
		super(name);
		methodList = new LinkedList<MethodCall>();
	}

	public void addMethodCall(MethodCall newMethodCall){
		methodList.add(newMethodCall);
	}
	
	public int getMethodNumber(){
		return methodList.size();
	}
	
	public MethodCall getMethodCall(int index){
		return methodList.get(index);
	}

}
