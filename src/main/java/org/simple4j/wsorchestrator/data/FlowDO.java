package org.simple4j.wsorchestrator.data;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowDO extends ValueRetriever
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ExecutionDO executionDO = null;
	private FlowDO parent = null;
	private String name = null;
	
	public FlowDO(ExecutionDO executionDO, FlowDO parent, File flowVariablesFile)
	{
		logger.info("Entering FlowDO {} {} {}", executionDO, parent, flowVariablesFile);
		this.executionDO = executionDO;
		this.parent = parent;
		this.name = flowVariablesFile.getParentFile().getName();
		if(this.parent != null)
		{
			this.parent.variables.put(name, this);
		}
		if(flowVariablesFile.exists())
		{
	    	variables = ConfigLoader.loadExecutionOrFlowVariables(flowVariablesFile , variables, "FLOW:");
		}
	}
	
	@Override
	public Object getVariableValue(String variableName)
	{
		logger.debug("getvariableValue {}", variableName);
		Object ret = null;
		if(variableName.contains("/"))
		{
			if(variableName.startsWith("../"))
			{
				variableName = variableName.substring(3);
				if(this.parent == null)
					throw new SystemException("PARENT_FLOWDO_NULL", "Trying to get variable value from parent of the root flow");
				return this.parent.getVariableValue(variableName);
			}
		}
		ret = super.getVariableValue(variableName);
		if(ret != null)
			return ret;
		return this.executionDO.getVariableValue(variableName);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [");
		builder.append("name=").append(name);
		builder.append(", variables=").append(variables);
		if(parent == null)
			builder.append(", executionDO=").append(executionDO);
		builder.append("]");
		return builder.toString();
	}

}
