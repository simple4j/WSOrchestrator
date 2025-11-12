package org.simple4j.wsorchestrator.model;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.FlowDO;

public interface Executable
{
	public void execute(ExecutionDO executionDO, FlowDO parent);
}
