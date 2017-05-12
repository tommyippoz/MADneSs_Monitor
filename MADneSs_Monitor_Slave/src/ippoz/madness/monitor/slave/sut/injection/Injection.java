/**
 * 
 */
package ippoz.madness.monitor.slave.sut.injection;

/**
 * @author root
 *
 */
public abstract class Injection {
	
	protected long delay;
	private Thread injThread;
	private long injMs;
	private String injType;
	private String injTag;
	
	public Injection(String injType, String injTag, long delay){
		this.injType = injType;
		this.injTag = injTag;
		this.delay = delay;
	}
	
	public String getInjType(){
		return injType;
	}
	
	public String getInjTag(){
		return injTag;
	}
	
	protected void setInjTimestamp(){
		injMs = System.currentTimeMillis();
	}
	
	protected void setInjTimestamp(long timestamp){
		injMs = timestamp;
	}
	
	public long getInjTimestamp(){
		return injMs;
	}
	
	public void setupInjection(){
		injThread = new Thread(new InjectionThread());	
		injThread.start();
	}
	
	protected abstract void inject();
	
	public void flushInjection(){
		flush();
		injThread.interrupt();
	}
	
	protected abstract void flush();
	
	private class InjectionThread implements Runnable {

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
