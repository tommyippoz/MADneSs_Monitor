/**
 * 
 */
package ippoz.multilayer.monitor.master.services;

/**
 * @author Tommy
 *
 */
public class MethodCall {
	
	private String portlet;
	private String method;
	
	public MethodCall(String portlet, String method) {
		this.portlet = portlet;
		this.method = method;
	}
	
	public String getPortlet() {
		return portlet;
	}
	
	public String getMethod() {
		return method;
	}

}
