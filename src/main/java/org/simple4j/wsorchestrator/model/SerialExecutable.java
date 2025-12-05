package org.simple4j.wsorchestrator.model;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates the logic for serial execution of sibling execution steps.
 */
public class SerialExecutable implements Executable
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private LinkedList<ExecutionStep> executionSteps = new LinkedList<ExecutionStep>();
	
	/**
	 * 
	 * @param executionSteps - list of execution steps that will get executed when this SerialExecutable is executed
	 */
	public SerialExecutable(LinkedList<ExecutionStep> executionSteps)
	{
		if(executionSteps == null)
			throw new SystemException("STEPS_NULL","steps is null");
		this.executionSteps.addAll(executionSteps);
	}

	public void execute(ExecutionDO executionDO, ExecutionFlowDO parent)
	{
		for(int i = 0 ; i < this.executionSteps.size() ; i++)
		{
			this.executionSteps.get(i).execute(executionDO, parent);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [executionSteps=").append(executionSteps).append("]");
		return builder.toString();
	}
}
