/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author root
 *
 */
public abstract class IteratingCommandProbe extends CycleProbe {
	
	private Thread commandThread; 
	private CommandRunner cRunner;
	protected String header;
	protected volatile String lastReaded;

	public IteratingCommandProbe(String command, String commandArgs, LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		super(probeLayer, probeName, receiverIp, probePort);
		cRunner = new CommandRunner(command, commandArgs);
		commandThread = new Thread(cRunner);
		commandThread.start();
	}

	protected abstract boolean isHeader(String line);

	@Override
	public void shutdownProbe() {
		cRunner.interruptCommand();
		super.shutdownProbe();
		if(!commandThread.isInterrupted())
			commandThread.interrupt();
	}

	private class CommandRunner implements Runnable {

		private String command;
		private String commandArgs;
		private Process process;
		private boolean halt;
		
		public CommandRunner(String command, String commandArgs) {
			this.command = command;
			this.commandArgs = commandArgs;
			halt = false;
		}

		@Override
		public void run() {
			BufferedReader reader = null;
			String script = command + " " + commandArgs;
			String readed;
			try {
				process = Runtime.getRuntime().exec(script);
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		        while (!halt && ((readed = reader.readLine()) != null)) {
		            if(header == null && isHeader(readed)) {
		            	header = readed;
		            } else {
		            	lastReaded = readed;
		            }
		        }
		        reader.close();	
		        process.destroy();
			} catch(Exception ex){
				AppLogger.logException(getClass(), ex, "");
			}
		}
		
		public void interruptCommand(){
			halt = true;
		}
		
	}

}
