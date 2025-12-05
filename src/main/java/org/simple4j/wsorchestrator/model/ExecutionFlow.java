package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class will be loaded at the time of application startup.
 * Flows and sub-flows are represented by instances of ExecutionFlow
 * 
 * ExecutionFlow represents an execution flow configured.
 * At the configuration level, it can contain other sub-flows as directories and/or execution steps as pair of input output properties.
 * The sequence of the flow is based on the natural sorting of the directories and files.
 * Execution steps will be executed in sequence and execution flows in parallel.
 * 
 * For example, if the natural sequence is 01-step1, 02-step2, 03-subflow1, 04-subfclow2, 05-step3, 06-step4, 07-subflow3, 08-subflow4
 * then 01-step1, 02-step2 will execute one after the other
 * then 03-subflow1, 04-subfclow2 will execute in parallel
 * then the execution flow ill wait for both sub flows to finish
 * then 05-step3, 06-step4 will execute one after the other
 * then 07-subflow3, 08-subfclow4 will execute in parallel
 * then the execution flow ill wait for both sub flows to finish
 * then the whole execution flow finishes
 * 
 */
public class ExecutionFlow implements Executable
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private File flowDirectory = null;
	private File flowVariablesFile = null;
	private LinkedList<Executable> executables = new LinkedList<Executable>();
	
	/**
	 * It takes the flow directory File instance to initialize the variables for the execution flow.
	 * It loop through the children and load the sub directories as ParallelExecutable sub-flows and
	 * load pairs of *-input.properties and *-output.properties as SerialExecutable steps.
	 * 
	 * @param flowDirectory
	 */
	public ExecutionFlow(File flowDirectory)
	{
		super();
		logger.info("Flow {}", flowDirectory);
		if(flowDirectory == null)
			throw new SystemException("FLOW_DIRECTORY_NULL","flowDirectory is null");
		if(!flowDirectory.isDirectory())
			throw new SystemException("FLOW_DIRECTORY_IS_NOT_DIRECTORY","flowDirectory is not a directory");
		if(!flowDirectory.exists())
			throw new SystemException("FLOW_DIRECTORY_DOESNOT_EXIST","flowDirectory does not exist");
		
		this.flowDirectory = flowDirectory;
		this.flowVariablesFile = new File(this.flowDirectory, "flowvariables.properties");
		this.loadConfigs();
	}

	private void loadConfigs()
	{
		logger.info("loadConfigs");
		File[] children = this.flowDirectory.listFiles();
		if(children == null || children.length == 0)
			return;

		List<File> sortedChildren = this.sortFiles(children);
		
		LinkedList<ExecutionFlow> executionFlows = new LinkedList<ExecutionFlow>();
		LinkedList<ExecutionStep> executionSteps = new LinkedList<ExecutionStep>();
		
		for(int i = 0 ; i < sortedChildren.size() ; i++)
		{
			File child = sortedChildren.get(i);
			logger.info("loading child {}", child);
			if(child.isDirectory())
			{
				logger.info("loading sub-directory {}", child);
				if(executionSteps.size() > 0)
				{
					executables.add(new SerialExecutable(executionSteps));
					executionSteps.clear();
				}
				executionFlows.add(new ExecutionFlow(child));
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
				if(executionFlows.size() > 0)
				{
					executables.add(new ParallelExecutable(executionFlows));
					executionFlows.clear();
				}
				executionSteps.add(ExecutionStep.getInstance(child));
			}
		}

		logger.info("executionSteps size {}", executionSteps.size());
		if(executionSteps.size() > 0)
		{
			executables.add(new SerialExecutable(executionSteps));
			executionSteps.clear();
		}
		logger.info("executables size {}", executables.size());

		logger.info("flows size {}", executionFlows.size());
		if(executionFlows.size() > 0)
		{
			executables.add(new ParallelExecutable(executionFlows));
			executionFlows.clear();
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

	/**
	 * Executes this ExecutionFlow without any parent flows.
	 * This means the current ExecutionFlow is a root execution flow without any parents.
	 * 
	 * @param executionDO
	 * @return
	 */
	public ExecutionFlowDO execute(ExecutionDO executionDO)
	{
		logger.info("Entering execute {}", executionDO);
		return executeInternal(executionDO, null);
	}
	

	/**
	 * Executes this ExecutionFlow with a parent flows.
	 * This means the current ExecutionFlow is a non-root execution flow.
	 * 
	 * @param executionDO - data object for the current execution
	 * @param parent - data object for the parent execution flow
	 */
	public void execute(ExecutionDO executionDO, ExecutionFlowDO parent)
	{
		logger.info("Entering execute {} {}", executionDO, parent);
		executeInternal(executionDO, parent);
	}
	
	private ExecutionFlowDO executeInternal(ExecutionDO executionDO, ExecutionFlowDO parent)
	{
		logger.info("Entering executeInternal {} {}", executionDO, parent);
		ExecutionFlowDO executionFlowDO = new ExecutionFlowDO(executionDO, parent, this.flowVariablesFile);
		
		for(int i=0 ; i < executables.size() ; i++)
		{
			executables.get(i).execute(executionDO, executionFlowDO);
		}
		return executionFlowDO;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [flowDirectory=").append(flowDirectory).append(", executables=")
				.append(executables).append("]");
		return builder.toString();
	}
}
