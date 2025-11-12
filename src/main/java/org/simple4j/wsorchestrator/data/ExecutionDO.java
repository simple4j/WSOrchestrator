package org.simple4j.wsorchestrator.data;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.simple4j.wsorchestrator.core.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class ExecutionDO extends ValueRetriever
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private File flowsDirectory = null;
	private ApplicationContext connectorsApplicationContext = null;
	
	public ExecutionDO(File flowsDirectory, ApplicationContext connectorsApplicationContext)
	{
		logger.info("Entering ExecutionDO {} {}", flowsDirectory, connectorsApplicationContext);
		
		if(flowsDirectory == null || !flowsDirectory.exists() || !flowsDirectory.isDirectory())
			throw new RuntimeException("flowsDirectory is null or not an existing directory :"+flowsDirectory);
		this.flowsDirectory = flowsDirectory;
		
		if(connectorsApplicationContext == null)
			throw new RuntimeException("connectorsApplicationContext is null");
		this.connectorsApplicationContext = connectorsApplicationContext;
		
		this.loadCustomVariables();
	}

	private void loadCustomVariables()
	{
        try
        {
            File executionVariablesFile = new File(this.flowsDirectory,"/executionvariables.properties");
        	variables = ConfigLoader.loadExecutionOrFlowVariables(executionVariablesFile , variables, "EXECUTION:");
		}
    	finally
    	{
    	}
	}

	public ApplicationContext getConnectorsApplicationContext()
	{
		return this.connectorsApplicationContext;
	}

	public File getFlowsDirectory()
	{
		return flowsDirectory;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [flowsDirectory=").append(flowsDirectory).append("]");
		return builder.toString();
	}

}
