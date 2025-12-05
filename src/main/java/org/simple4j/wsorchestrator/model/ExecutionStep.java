package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to represent an execution step.
 * This can be extended to create more specific step types.
 */
public abstract class ExecutionStep implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected File inputFile = null;
	protected File outputFile = null;

	/**
	 * 
	 * @param inputFile - the File object of the *-input.properties file for this execution step
	 */
	public ExecutionStep(File inputFile)
	{
		//below files are loaded at start up to avoid file io exceptions at runtime
		ConfigLoader.loadProperties(inputFile);
		File outputFile = new File(inputFile.getParentFile(), inputFile.getName().replace("-input.properties", "-output.properties"));
		if(outputFile.exists())
		{
			ConfigLoader.loadProperties(outputFile);
			this.outputFile = outputFile;
		}
		
		this.inputFile = inputFile;
	}

	/**
	 * Abstract method which will have the logic to execute the execution step
	 */
	public abstract void execute(ExecutionDO execution, ExecutionFlowDO parent);

	/**
	 * Factory method to encapsulate the logic of identifying the specific sub-type of ExecutionStep.
	 * This is typically done based on the prefix to -input.properties
	 * 
	 * @param inputFile - the File object of the *-input.properties file for which an instance of ExecutionType will be returned
	 * @return
	 */
	public static ExecutionStep getInstance(File inputFile)
	{
		logger.info("Entering getInstance {}", inputFile);
		ExecutionStep ret = null;
		if(inputFile.getName().endsWith("-ws-input.properties"))
			ret = new WSExecutionStep(inputFile);
		logger.info("Exiting getInstance {}", ret);
		return ret ;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [inputFile=").append(inputFile).append("]");
		return builder.toString();
	}
}
