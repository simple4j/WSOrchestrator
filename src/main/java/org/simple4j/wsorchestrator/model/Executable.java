package org.simple4j.wsorchestrator.model;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;

/**
 * This interface will be implemented by all the executable classes to standardize the execute method signature
 */
public interface Executable
{
	/**
	 * 
	 * @param executionDO - data object for the current execution
	 * @param parent - data object for the parent execution flow
	 */
	public void execute(ExecutionDO executionDO, ExecutionFlowDO parent);
}
