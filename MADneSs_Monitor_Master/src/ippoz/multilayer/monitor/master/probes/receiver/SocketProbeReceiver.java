/**
 * 
 */
package ippoz.multilayer.monitor.master.probes.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;
import ippoz.multilayer.monitor.master.observation.ObservationCollector;

/**
 * @author Tommy
 *
 */
public abstract class SocketProbeReceiver extends ProbeReceiver{

	private ServerSocket ssocket;
	private boolean continuous;
	
	public SocketProbeReceiver(String receiverName, ObservationCollector collector, LayerType type, int port, long msDelay, boolean continuous) throws IOException {
		super(receiverName, collector, type, msDelay);
		this.continuous = continuous;
		ssocket = new ServerSocket(port);
	}
	
	@Override
	public boolean testConnection() {
		try {
			ssocket.accept();
			return true;
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to get response for probe " + getReceiverName());
		}
		return false;
	}

	@Override
	public boolean canRead() {
		return !ssocket.isClosed();
	}
	
	@Override
	public void startReading(){
		try {
			if(continuous)
				readContinuous();
			else readNonContinuous();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Probe read ended with errors");
		}
	}
	
	public void readContinuous() throws IOException, ClassNotFoundException {
		Socket clientSocket = null;
		ObjectInputStream inStream = null;
		try {
			//AppLogger.logInfo(getClass(), "Receiver ready on port " + ssocket.getLocalPort());
			clientSocket = ssocket.accept();                       
	        inStream = new ObjectInputStream(clientSocket.getInputStream());
	        while (canRead()) {
            	collector.addObservation(parseObservation(inStream));
            } 
		} finally {
			try {
				inStream.close();
				if(!clientSocket.isClosed())
					clientSocket.close();
				//AppLogger.logInfo(getClass(), "Probe " + getReceiverName() + ": collected " + nObs + " observations");
			}catch(Exception ex){}
		}
	}
	
	public void readNonContinuous() throws IOException{
		//int nObs = 0;
		Socket clientSocket = null;
		InputStream inStream = null;
		try {
			while (canRead()) {
				//AppLogger.logInfo(getClass(), "Receiver ready on port " + ssocket.getLocalPort());
				clientSocket = ssocket.accept();                       
		        inStream = clientSocket.getInputStream();
		        if(collector.addObservation(parseObservation(inStream))){
            		// nObs++;
		        }
            	inStream.close();
            	clientSocket.close();
            } 
		} finally {
			inStream.close();
			if(!clientSocket.isClosed())
				clientSocket.close();
			//AppLogger.logInfo(getClass(), "Probe " + getReceiverName() + ": collected " + nObs + " observations");
		}
	}

	@Override
	public void endReading() {
		try {
			ssocket.close();
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to close receiver in a proper way");
		}
	}

}
