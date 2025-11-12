package org.simple4j.wsorchestrator.model;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelExecutable implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public static int maxThreadPoolSize = 10;

	private LinkedList<Flow> flows = new LinkedList<Flow>();
	private ExecutorService executorService = null;

	public ParallelExecutable(LinkedList<Flow> flows)
	{
		if (flows != null)
			this.flows.addAll(flows);
		int threadPoolSize = this.flows.size() < this.maxThreadPoolSize ? this.flows.size() : this.maxThreadPoolSize;
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void execute(ExecutionDO executionDO, FlowDO parent)
	{
		LinkedList<Future<Void>> futures = new LinkedList<Future<Void>>();
		for(int i = 0 ; i < this.flows.size() ; i++)
		{
			Future<Void> future = this.executorService.submit(new FlowExecutor(executionDO, parent, this.flows.get(i)));
			futures.add(future);
		}

		for(int i = 0 ; i < futures.size() ; i++)
		{
            try
			{
				futures.get(i).get();
			} catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			} catch (ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [flows=").append(flows).append("]");
		return builder.toString();
	}
}
