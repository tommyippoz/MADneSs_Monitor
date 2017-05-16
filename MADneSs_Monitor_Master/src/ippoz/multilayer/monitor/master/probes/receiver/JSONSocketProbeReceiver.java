/**
 * 
 */
package ippoz.multilayer.monitor.master.probes.receiver;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.multilayer.monitor.master.observation.Observation;
import ippoz.multilayer.monitor.master.observation.ObservationCollector;
import ippoz.multilayer.monitor.master.performance.ExperimentTiming;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Tommy
 *
 */
public class JSONSocketProbeReceiver extends SocketProbeReceiver {

	public static final String ARRAY_TAG = "observations";
	public static final String INDICATOR_TAG = "attributeName";
	public static final String DESCRIPTION_TAG = "attributeDesc";
	public static final String VALUE_TAG = "attributeValue";
	public static final String TIMESTAMP_TAG = "time_ms";
	public static final String PROCESSING_TIME_TAG = "processingTime_ms";
	public static final String DELIVERY_TIME_MS = "delivery_time_ms";
	
	private DateFormat formatter;
	private long obsArrivalTime;
	
	public JSONSocketProbeReceiver(String receiverName, ObservationCollector collector, LayerType type, int port, long msDelay, boolean continuous) throws IOException {
		super(receiverName, collector, type, port, msDelay, continuous);
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public Observation parseObservation(InputStream inStream) {
		JSONObject jObj = readJSON(inStream);
		if(jObj != null){
			return parseJSON(jObj);
		}
		else return null;
	}

	private Observation parseJSON(JSONObject jObj) {
		Observation obs = null;
		JSONArray array = null;
		array = jObj.getJSONArray(ARRAY_TAG);
		obs = new Observation(new Date(Long.valueOf(jObj.getString(TIMESTAMP_TAG)) + getMsDelay()), formatter.format(new Date(Long.valueOf(jObj.getString(TIMESTAMP_TAG)))));
		for(int i=0;i<array.size();i++){
			if(!obs.hasIndicator(array.getJSONObject(i).getString(INDICATOR_TAG)))
			obs.addIndicator(new Indicator(array.getJSONObject(i).getString(INDICATOR_TAG), getLayerType(), String.class, array.getJSONObject(i).getString(DESCRIPTION_TAG)), array.getJSONObject(i).getString(VALUE_TAG));
		}
		ExperimentTiming.setProbeObservation(this, jObj.getJSONArray(ARRAY_TAG).size(), jObj.getInt(PROCESSING_TIME_TAG), (int)(obsArrivalTime - jObj.getLong(DELIVERY_TIME_MS)), (int)(System.currentTimeMillis() - obsArrivalTime));
		return obs;
	}

	private JSONObject readJSON(InputStream inStream) {
		String readed;
		JSONObject jObj = null;
        try {
        	readed = (String)((ObjectInputStream)inStream).readObject();
        	obsArrivalTime = System.currentTimeMillis();
        	jObj = JSONObject.fromObject(readed);
		} catch(Exception ex){
			AppLogger.logError(getClass(), "EndOfStream", "Detected end of stream");
			endReading();
		}
        return jObj;
	}

}
