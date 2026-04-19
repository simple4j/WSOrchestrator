package org.simple4j.wsorchestrator.model;

import java.util.Map;
import java.util.concurrent.Callable;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.slf4j.MDC;

/**
 * This is a Callable wrapper for ExecutionFlow to enable java executor service
 * to execute execution flow in a separate thread from a thread pool
 */
public class ExecutionFlowExecutor implements Callable<Void>
{
	private ExecutionDO executionDO = null;
	private ExecutionFlowDO executionFlowDO = null;
	private ExecutionFlow flow = null;
	private Map<String, String> loggerContextMap = null;
	
	/**
	 * 
	 * @param executionDO - data object for the current execution
	 * @param executionFlowDO - data object for the execution flow that will be executed by this ExecutionFlowExecutor
	 * @param flow - execution flow object that will be executed by this ExecutionFlowExecutor
	 * @param loggerContextMap 
	 */
	public ExecutionFlowExecutor(ExecutionDO executionDO, ExecutionFlowDO executionFlowDO, ExecutionFlow flow, Map<String, String> loggerContextMap)
	{
		super();
		this.executionDO = executionDO;
		this.executionFlowDO = executionFlowDO;
		this.flow = flow;
		this.loggerContextMap = loggerContextMap;
	}

	@Override
	public Void call() throws Exception
	{
		MDC.setContextMap(this.loggerContextMap);
		try
		{
			this.flow.execute(executionDO, executionFlowDO);
			return null;
		}
		finally
		{
			MDC.clear();
		}
	}

}
