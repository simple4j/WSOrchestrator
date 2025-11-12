package org.simple4j.wsorchestrator.model;

import java.util.concurrent.Callable;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;

public class FlowExecutor implements Callable<Void>
{
	private ExecutionDO executionDO = null;
	private FlowDO flowDO = null;
	private Flow flow = null;
	public FlowExecutor(ExecutionDO executionDO, FlowDO flowDO, Flow flow)
	{
		this.executionDO = executionDO;
		this.flowDO = flowDO;
		this.flow = flow;
	}

	@Override
	public Void call() throws Exception
	{
		this.flow.execute(executionDO, flowDO);
		return null;
	}

}
