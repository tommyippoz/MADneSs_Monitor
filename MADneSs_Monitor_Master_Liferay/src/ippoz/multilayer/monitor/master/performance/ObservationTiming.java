/**
 * 
 */
package ippoz.multilayer.monitor.master.performance;

/**
 * @author tommaso
 *
 */
public class ObservationTiming {
	
	private int indNumber;
	private int ot;
	private int pmtt;
	private int dat;
	
	public ObservationTiming(int indNumber, int ot, int pmtt, int dat) {
		this.indNumber = indNumber;
		this.ot = ot;
		this.pmtt = pmtt;
		this.dat = dat;
	}
	
	public int getIndNumber(){
		return indNumber;
	}

	public int getOt() {
		return ot;
	}

	public int getPmtt() {
		return pmtt;
	}

	public int getDat() {
		return dat;
	}

}
