package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.simple4j.wsorchestrator.core.ConfigLoader;
import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Step implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected File inputFile = null;
	protected File outputFile = null;

	public Step(File inputFile)
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

	public abstract void execute(ExecutionDO execution, FlowDO parent);
	
	public static Step getInstance(File inputFile)
	{
		logger.info("Entering getInstance {}", inputFile);
		Step ret = null;
		if(inputFile.getName().endsWith("-ws-input.properties"))
			ret = new WSStep(inputFile);
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
