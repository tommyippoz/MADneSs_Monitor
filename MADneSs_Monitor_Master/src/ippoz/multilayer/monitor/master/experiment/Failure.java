/**
 * 
 */
package ippoz.multilayer.monitor.master.experiment;

/**
 * @author tommaso
 *
 */
public class Failure {
	
	private String failureType;
	private String failureTag;
	private String failureDetails;
	private Long failureActivation;
	
	public Failure(String failureType, String failureTag, String failureDetails) {
		this.failureType = failureType;
		this.failureTag = failureTag;
		this.failureDetails = failureDetails;
	}
	
	public String getFailureDetails(){
		return failureDetails;
	}

	public Long getFailureActivation() {
		return failureActivation;
	}

	public void setFailureActivation(Long failureActivation) {
		this.failureActivation = failureActivation;
	}

	public String getFailureType() {
		return failureType;
	}

	public String getFailureTag() {
		return failureTag;
	}

}
