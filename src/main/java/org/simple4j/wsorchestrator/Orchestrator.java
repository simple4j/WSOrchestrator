package org.simple4j.wsorchestrator;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.model.ExecutionFlow;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The instance of this class is created at the application startup and it will represent an orchestrator with one or more flows. 
 */

public class Orchestrator
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private HashMap<String, ExecutionFlow> executioFlows = new HashMap<String, ExecutionFlow>();
	private File flowsRootDirectory = null; 
	private ApplicationContext connectorsApplicationContext = null;
	
	/**
	 * 
	 */
	public Orchestrator(File flowsRootDirectory, ApplicationContext connectorsApplicationContext)
	{
		super();

		if(flowsRootDirectory == null)
			throw new SystemException("FLOWS_ROOT_DIRECTORY_NULL","flowsRootDirectory is null");
		
		if(connectorsApplicationContext == null)
			throw new SystemException("CONNECTORAPPLICATIONCONTEXT_NULL", "connectorsApplicationContext is null");
		
		this.flowsRootDirectory = flowsRootDirectory;
		this.connectorsApplicationContext = connectorsApplicationContext;
		List<File> flowDirectories = ConfigLoader.getChildrenDirectories(flowsRootDirectory);
		for(int i = 0 ; i < flowDirectories.size() ; i++)
		{
			this.executioFlows.put(flowDirectories.get(i).getName(), new ExecutionFlow(flowDirectories.get(i)));
		}
		
		logger.info("Loaded flows :  {}", this.executioFlows);
	}
	
	/**
	 * Executes an execution flow and returns the data object
	 * 
	 * @param flowDirectory - flow directory name from the flowsRootDirectory location
	 * @param executionParameters - any parameters to be passed to the execution that will be used as variables any step 
	 * @return
	 */
	public ExecutionFlowDO execute(String flowDirectory, Map<String, Object> executionParameters)
	{
		ExecutionFlowDO ret = null;
		ExecutionFlow executionFlow = this.getExecutionFlow(flowDirectory);
		if(executionFlow == null)
			throw new SystemException("FLOW_NOT_FOUND", "Flow not found:"+flowDirectory);
		
		ExecutionDO executionDO = new ExecutionDO(this.flowsRootDirectory, this.connectorsApplicationContext, executionParameters);
		ret = executionFlow.execute(executionDO);
		return ret ;
	}

	private ExecutionFlow getExecutionFlow(String flowDirectory)
	{
		return executioFlows.get(flowDirectory);
	}
}
