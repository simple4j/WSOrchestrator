package org.simple4j.wsorchestrator.model;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialExecutable implements Executable
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private LinkedList<Step> steps = new LinkedList<Step>();
	
	public SerialExecutable(LinkedList<Step> steps)
	{
		if(steps == null)
			throw new RuntimeException("steps is null");
		this.steps.addAll(steps);
	}

	public void execute(ExecutionDO executionDO, FlowDO parent)
	{
		for(int i = 0 ; i < this.steps.size() ; i++)
		{
			this.steps.get(i).execute(executionDO, parent);
		}
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [steps=").append(steps).append("]");
		return builder.toString();
	}
}
