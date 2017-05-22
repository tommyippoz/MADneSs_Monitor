/**
 * 
 */
package ippoz.madness.monitor.slave.probes;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// TODO: Auto-generated Javadoc
/**
 * The Class IteratingCommandProbe.
 *
 * @author ippoz
 */
public abstract class IteratingCommandProbe extends CycleProbe {
	
	/** The command thread. */
	private Thread commandThread; 
	
	/** The command runner. */
	private CommandRunner cRunner;
	
	/** The header of the probe. */
	protected String header;
	
	/** The last read value. */
	protected volatile String lastReaded;

	/**
	 * Instantiates a new iterating command probe.
	 *
	 * @param command the command
	 * @param commandArgs the command arguments
	 * @param probeLayer the probe layer
	 * @param probeName the probe name
	 * @param receiverIp the receiver IP address
	 * @param probePort the probe port
	 */
	public IteratingCommandProbe(String command, String commandArgs, LayerType probeLayer, String probeName, String receiverIp, int probePort) {
		super(probeLayer, probeName, receiverIp, probePort);
		cRunner = new CommandRunner(command, commandArgs);
		commandThread = new Thread(cRunner);
		commandThread.start();
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.Probe#canRun()
	 */
	@Override
	public boolean canRun() {
		return commandThread != null;
	}

	/**
	 * Checks if it is an header row.
	 *
	 * @param line the line
	 * @return true, if it is an header row
	 */
	protected abstract boolean isHeader(String line);

	/* (non-Javadoc)
	 * @see ippoz.madness.monitor.slave.probes.CycleProbe#shutdownProbe()
	 */
	@Override
	public void shutdownProbe() {
		cRunner.interruptCommand();
		super.shutdownProbe();
		if(!commandThread.isInterrupted())
			commandThread.interrupt();
	}

	/**
	 * The Class CommandRunner.
	 */
	private class CommandRunner implements Runnable {

		/** The command. */
		private String command;
		
		/** The command args. */
		private String commandArgs;
		
		/** The process. */
		private Process process;
		
		/** The halt. */
		private boolean halt;
		
		/**
		 * Instantiates a new command runner.
		 *
		 * @param command the command
		 * @param commandArgs the command args
		 */
		public CommandRunner(String command, String commandArgs) {
			this.command = command;
			this.commandArgs = commandArgs;
			halt = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
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
		
		/**
		 * Interrupt command.
		 */
		public void interruptCommand(){
			halt = true;
		}
		
	}

}
