package org.simple4j.wsorchestrator.data;

import java.io.File;

import org.simple4j.wsorchestrator.util.ConfigLoader;

/**
 * This will hold the data of a loop. This does not represent the iterations in the loop.
 * Even though the model is LoopsTep, the LoopDO is extention of ExecutionFlowDO as this DO is not a leaf node.
 */
public class LoopDO extends ExecutionFlowDO
{
	private File inputFile=null;

	public LoopDO(ExecutionDO executionDO, ExecutionFlowDO parent, File inputFile)
	{
		super(executionDO, parent);
		String stepAbsolutePath = inputFile.getAbsolutePath();
		this.name = stepAbsolutePath.substring(executionDO.getFlowsRootDirectory().getAbsolutePath().length(),stepAbsolutePath.length()-"-input.properties".length());
		this.inputFile = inputFile;
		this.variables = ConfigLoader.loadStepVariables(inputFile, parent);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [inputFile=").append(inputFile).append("]");
		return builder.toString();
	}

}
