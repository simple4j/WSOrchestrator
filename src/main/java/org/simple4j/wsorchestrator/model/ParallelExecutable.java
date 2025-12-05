package org.simple4j.wsorchestrator.model;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates the logic for parallel execution of sibling execution flows.
 * 
 */
public class ParallelExecutable implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/**
	 * Maximum thread pool size.
	 * This needs to be set before creating an instance of ParallelExecutable.
	 * This is not threadsafe and multiple threads setting this value and instantiating may not give accurate size.
	 */
	public static int maxThreadPoolSize = 10;

	private LinkedList<ExecutionFlow> flows = new LinkedList<ExecutionFlow>();
	private ExecutorService executorService = null;

	/**
	 * 
	 * @param executionFlows - list of execution flows that will get executed when this ParallelExecutable is executed
	 */
	public ParallelExecutable(LinkedList<ExecutionFlow> executionFlows)
	{
		if (executionFlows != null)
			this.flows.addAll(executionFlows);
		int threadPoolSize = this.flows.size() < this.maxThreadPoolSize ? this.flows.size() : this.maxThreadPoolSize;
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void execute(ExecutionDO executionDO, ExecutionFlowDO parent)
	{
		LinkedList<Future<Void>> futures = new LinkedList<Future<Void>>();
		for(int i = 0 ; i < this.flows.size() ; i++)
		{
			Future<Void> future = this.executorService.submit(new ExecutionFlowExecutor(executionDO, parent, this.flows.get(i)));
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
