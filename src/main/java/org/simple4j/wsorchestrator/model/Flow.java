package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.simple4j.wsorchestrator.core.ConfigLoader;
import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Flow implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private File flowDir = null;
	private File flowVariablesFile = null;
	private LinkedList<Executable> executables = new LinkedList<Executable>();
	
	public Flow(File flowDir)
	{
		logger.info("Flow {}", flowDir);
		if(flowDir == null)
			throw new RuntimeException("flowDir is null");
		if(!flowDir.isDirectory())
			throw new RuntimeException("flowDir is not a directory");
		if(!flowDir.exists())
			throw new RuntimeException("flowDir does not exist");
		
		this.flowDir = flowDir;
		this.loadConfigs();
	}

	private void loadConfigs()
	{
		logger.info("loadConfigs");
		File[] children = this.flowDir.listFiles();
		if(children == null || children.length == 0)
			return;

		List<File> sortedChildren = this.sortFiles(children);
		
		LinkedList<Flow> flows = new LinkedList<Flow>();
		LinkedList<Step> steps = new LinkedList<Step>();
		
		for(int i = 0 ; i < sortedChildren.size() ; i++)
		{
			File child = sortedChildren.get(i);
			logger.info("loading child {}", child);
			if(child.isDirectory())
			{
				logger.info("loading sub-directory {}", child);
				if(steps.size() > 0)
				{
					executables.add(new SerialExecutable(steps));
					steps.clear();
				}
				flows.add(new Flow(child));
			}
			if(child.getName().equals("flowvariables.properties"))
			{
				logger.info("loading flowcariables {}", child);
				this.flowVariablesFile = child;
				// loading properties to make sure file is good and avoid error while executing
				Properties flowVariables = new Properties();
				flowVariables.putAll(ConfigLoader.loadProperties(child));
			}
			if(child.getName().endsWith("-input.properties"))
			{
				logger.info("loading step {}", child);
				if(flows.size() > 0)
				{
					executables.add(new ParallelExecutable(flows));
					flows.clear();
				}
				steps.add(Step.getInstance(child));
			}
		}

		logger.info("steos size {}", steps.size());
		if(steps.size() > 0)
		{
			executables.add(new SerialExecutable(steps));
			steps.clear();
		}
		logger.info("executables size {}", executables.size());

		logger.info("flows size {}", flows.size());
		if(flows.size() > 0)
		{
			executables.add(new ParallelExecutable(flows));
			flows.clear();
		}
		logger.info("executables size {}", executables.size());

	}

	private List<File> sortFiles(File[] unsortedFiles) {
        List<File> sortedFiles = null;
        if(unsortedFiles != null)
        {
            sortedFiles = Arrays.asList(unsortedFiles);
            Comparator<? super File> c = new Comparator<File>() {

                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            Collections.sort(sortedFiles, c );

        }
        return sortedFiles;
    }

	public FlowDO execute(ExecutionDO executionDO)
	{
		logger.info("Entering execute {}", executionDO);
		return executeInternal(executionDO, null);
	}
	
	public void execute(ExecutionDO executionDO, FlowDO parent)
	{
		logger.info("Entering execute {} {}", executionDO, parent);
		executeInternal(executionDO, parent);
	}
	
	private FlowDO executeInternal(ExecutionDO executionDO, FlowDO parent)
	{
		logger.info("Entering executeInternal {} {}", executionDO, parent);
		FlowDO flowDO = new FlowDO(executionDO, parent, this.flowVariablesFile);
		
		for(int i=0 ; i < executables.size() ; i++)
		{
			executables.get(i).execute(executionDO, flowDO);
		}
		return flowDO;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [flowDir=").append(flowDir).append(", executables=")
				.append(executables).append("]");
		return builder.toString();
	}
}
