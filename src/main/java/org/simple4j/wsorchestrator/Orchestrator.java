package org.simple4j.wsorchestrator;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.model.Flow;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class Orchestrator
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private HashMap<String, Flow> flows = new HashMap<String, Flow>();
	private File flowsDir = null; 
	private ApplicationContext connectorsApplicationContext = null;
	public Orchestrator(File flowsDir, ApplicationContext connectorsApplicationContext)
	{
		super();

		if(flowsDir == null)
			throw new SystemException("FLOWS_DIRECTORY_NULL","flowsDir is null");
		
		if(connectorsApplicationContext == null)
			throw new SystemException("CONNECTORAPPLICATIONCONTEXT_NULL", "connectorsApplicationContext is null");
		
		this.flowsDir = flowsDir;
		this.connectorsApplicationContext = connectorsApplicationContext;
		List<File> flowDirs = ConfigLoader.getChildrenDirectories(flowsDir);
		for(int i = 0 ; i < flowDirs.size() ; i++)
		{
			this.flows.put(flowDirs.get(i).getName(), new Flow(flowDirs.get(i)));
		}
		
		logger.info("Loaded flows :  {}", this.flows);
	}
	
	public FlowDO execute(String flowDir)
	{
		FlowDO ret = null;
		Flow flow = this.getFlow(flowDir);
		if(flow == null)
			throw new SystemException("FLOW_NOT_FOUND", "Flow not found:"+flowDir);
		ExecutionDO executionDO = new ExecutionDO(this.flowsDir, this.connectorsApplicationContext);
		ret = flow.execute(executionDO);
		return ret ;
	}

	public Flow getFlow(String flowDir)
	{
		return flows.get(flowDir);
	}
}
