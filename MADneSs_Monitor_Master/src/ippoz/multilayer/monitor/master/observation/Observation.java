/**
 * 
 */
package ippoz.multilayer.monitor.master.observation;

import java.util.Date;
import java.util.HashMap;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.support.AppLogger;

/**
 * @author Tommy
 *
 */
public class Observation {
	
	private Date datetime;
	private String timestamp;
	private HashMap<Indicator, String> observedIndicators;
	
	public Observation(Date datetime, String timestamp){
		this.timestamp = timestamp;
		observedIndicators = new HashMap<Indicator, String>();
	}
	
	public void addIndicator(Indicator newInd, String newValue){
		observedIndicators.put(newInd, newValue);
	}

	public String getTimestamp() {
		return timestamp;
	}
	
	public Date getDateTimestamp() {
		return datetime;
	}
	
	public Indicator[] getIndicators(){
		return observedIndicators.keySet().toArray(new Indicator[observedIndicators.keySet().size()]);
	}

	public String getValue(Indicator indicator) {
		String retValue = observedIndicators.get(indicator);
		if(retValue != null && !retValue.equals("null"))
			return retValue;
		else {
			for(Indicator ind : observedIndicators.keySet()){
				if(ind.compareTo(indicator) == 0)
					return getValue(ind);
			}
		}
		return null;
	}
	
	public int size(){
		return observedIndicators.size();
	}

	public void mergeWith(Observation obs) {
		if(obs.getTimestamp().equals(getTimestamp())){
			observedIndicators.putAll(obs.getIndicatorMap());
		} else AppLogger.logError(getClass(), "TimeStampNotMatching", "Unable to merge observations");
	}

	private HashMap<Indicator, String> getIndicatorMap() {
		return observedIndicators;
	}

	public String getValue(String indName) {
		for(Indicator ind : observedIndicators.keySet()){
			if(ind.getName().compareTo(indName) == 0)
				return getValue(ind);
		}
		return null;
	}

	public boolean hasIndicator(String indName) {
		for(Indicator ind : observedIndicators.keySet()){
			if(ind.getName().equals(indName))
				return true;
		}
		return false;
	}

}
