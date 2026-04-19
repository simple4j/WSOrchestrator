package org.simple4j.wsorchestrator.data;

import java.io.File;

import org.simple4j.wsorchestrator.exception.SystemException;

public class LoopIterationDO extends ExecutionFlowDO
{
	
	private ExecutionDO executionDO=null;
	private LoopDO parent=null;
	private Object loopItem=null;
	private int index=-1;

	public LoopIterationDO(ExecutionDO executionDO, LoopDO parent, Object loopItem, int index)
	{
		super(executionDO, parent);
		if(executionDO == null)
			throw new SystemException("EXECUTIONDO_NULL", "executionDO is null");
		else
			this.executionDO = executionDO;
			
		if(parent == null)
			throw new SystemException("PARENT_NULL", "parent is null");
		else
			this.parent = parent;
			
		if(loopItem == null)
			throw new SystemException("INPUTFILE_NULL", "loopItem is null");
		else
			this.loopItem = loopItem;
			
		if(index < 0)
			throw new SystemException("INDEX_INVALID", "index is less than zero");
		else
			this.index = index;
			
		this.name = this.parent.name;
		this.name = this.name + "[" + index + "]";
		
		this.parent.variables.put(this.name, this);
		
		this.variables.put("LOOP_ITEM", loopItem);
		this.variables.put("LOOP_INDEX", index);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" [loopItem=").append(loopItem).append(", index=").append(index)
				.append("]");
		return builder.toString();
	}

	
}
