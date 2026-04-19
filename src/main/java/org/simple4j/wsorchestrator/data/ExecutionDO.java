package org.simple4j.wsorchestrator.data;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.model.ExecutionFlow;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * This will hold the data of an execution (or invocation) of an execution flow.
 */
public class ExecutionDO extends ValueRetriever
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private File flowsRootDirectory = null;
	private ApplicationContext connectorsApplicationContext = null;
	private String entryFlowDirectory = null;
	private Map<String, ExecutionFlow> entryExecutionFlows = null;
	
	/**
	 * 
	 * @param flowsRootDirectory - the root directory where all the flows are defined
	 * @param connectorsApplicationContext - application context from which the needed beans are retrieved for each of the execution steps
	 * @param executionParameters TODO
	 * @param entryExecutionFlows 
	 * @param entryFlowDirectory 
	 */
	public ExecutionDO(File flowsRootDirectory, ApplicationContext connectorsApplicationContext, Map<String, Object> executionParameters,
			String entryFlowDirectory, Map<String, ExecutionFlow> entryExecutionFlows)
	{
		logger.info("Entering ExecutionDO {} {}", flowsRootDirectory, connectorsApplicationContext);
		
		if(flowsRootDirectory == null || !flowsRootDirectory.exists() || !flowsRootDirectory.isDirectory())
			throw new SystemException("FLOWS_DIRECTORY_NULL_NOT_EXISTSS", "flowsRootDirectory is null or not an existing directory :"+flowsRootDirectory);
		this.flowsRootDirectory = flowsRootDirectory;
		
		if(connectorsApplicationContext == null)
			throw new SystemException("CONNECTORAPPLICATIONCONTEXT_NULL", "connectorsApplicationContext is null");
		this.connectorsApplicationContext = connectorsApplicationContext;
		
		if(entryFlowDirectory == null || entryFlowDirectory.trim().length() < 1)
		{
			throw new SystemException("ENTRY_FLOW_DIRECTORY_NULL", "entryFlowsRootDirectory is null or empty :"+entryFlowDirectory);
		}
		this.entryFlowDirectory = entryFlowDirectory;
		
		if(entryExecutionFlows == null || entryExecutionFlows.size() < 1)
		{
			throw new SystemException("ENTRY_EXECUTION_FLOWS_NULL", "entryExecutionFlows is null or empty :"+entryExecutionFlows);
		}
		this.entryExecutionFlows = entryExecutionFlows;

		this.loadCustomVariables();
		if(executionParameters != null)
			variables.putAll(executionParameters);
	}

	private void loadCustomVariables()
	{
        try
        {
            File executionVariablesFile = new File(this.flowsRootDirectory,"/executionvariables.properties");
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

	public File getFlowsRootDirectory()
	{
		return flowsRootDirectory;
	}

	public String getEntryFlowDirectory()
	{
		return entryFlowDirectory;
	}

	public Map<String, ExecutionFlow> getEntryExecutionFlows()
	{
		return entryExecutionFlows;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [flowsRootDirectory=").append(flowsRootDirectory).append("]");
		return builder.toString();
	}

}
