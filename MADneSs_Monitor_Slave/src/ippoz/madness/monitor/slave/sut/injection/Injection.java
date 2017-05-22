/**
 * 
 */
package ippoz.madness.monitor.slave.sut.injection;

import ippoz.madness.commons.support.AppLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Injection.
 *
 * @author root
 */
public abstract class Injection {
	
	/** The injection delay. */
	protected long delay;
	
	/** The injection thread. */
	private Thread injThread;
	
	/** The injection ms. */
	private long injMs;
	
	/** The injection detail. */
	private String injDetail;
	
	/** The injection tag. */
	private String injTag;
	
	/**
	 * Instantiates a new injection.
	 *
	 * @param injTag the injection tag
	 * @param delay the injection delay
	 */
	public Injection(String injTag, long delay){
		this.injTag = injTag;
		this.delay = delay;
	}
	
	/**
	 * Gets the injection tag.
	 *
	 * @return the injection tag
	 */
	public String getInjTag(){
		return injTag;
	}
	
	/**
	 * Sets the injection timestamp.
	 */
	protected void setInjTimestamp(){
		injMs = System.currentTimeMillis();
	}
	
	/**
	 * Sets the injection timestamp.
	 *
	 * @param timestamp the injection timestamp
	 * @param injDetail the injection detail
	 */
	protected void setInjTimestamp(Long timestamp, String injDetail){
		if(timestamp != null){
			this.injDetail = injDetail;
			injMs = timestamp;
		} else AppLogger.logError(getClass(), "InjectionNotActivated", toString());
	}
	
	/**
	 * Gets the injection timestamp.
	 *
	 * @return the injection timestamp
	 */
	public long getInjTimestamp(){
		return injMs;
	}
	
	/**
	 * Gets the injection detail.
	 *
	 * @return the injection detail
	 */
	public String getInjDetail(){
		return injDetail;
	}
	
	/**
	 * Setup injection.
	 */
	public void setupInjection(){
		injThread = new Thread(new InjectionThread());	
		injThread.start();
	}
	
	/**
	 * Inject.
	 */
	protected abstract void inject();
	
	/**
	 * Flush injection.
	 */
	public void flushInjection(){
		flush();
		injThread.interrupt();
	}
	
	/**
	 * Flush.
	 */
	protected abstract void flush();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Injection [delay=" + delay + ", injThread=" + injThread
				+ ", injMs=" + injMs + ", injDetail=" + injDetail + ", injTag="
				+ injTag + "]";
	}

	/**
	 * The Class InjectionThread.
	 */
	private class InjectionThread implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				Thread.sleep(delay);
				inject();
				setInjTimestamp();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	

}
