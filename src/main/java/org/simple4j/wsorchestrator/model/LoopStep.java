package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.data.LoopDO;
import org.simple4j.wsorchestrator.data.LoopIterationDO;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopStep extends ExecutionStep
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public LoopStep(File inputFile)
	{
		super(inputFile);
	}

	@Override
	public void execute(ExecutionDO executionDO, ExecutionFlowDO parentExecutionFlowDO)
	{
//		ExecutionStepDO executionStepDO = new ExecutionStepDO(executionDO, parentExecutionFlowDO, this.inputFile, this.outputFile);
		LoopDO loopDO = new LoopDO(executionDO, parentExecutionFlowDO, this.inputFile);
		if(!loopDO.canExecute())
		{
			return;
		}

    	Object loopListObj = loopDO.getVariableValue("LOOP_LIST");
    	if(loopListObj == null)
    	{
    		logger.info("LOOP_LIST value is null");
    		return;
    	}
    	
    	Object loopBodyFlowName = loopDO.getVariableValue("LOOP_BODY_FLOW_NAME");
    	if(loopBodyFlowName == null)
    	{
    		throw new SystemException("LOOP_BODY_FLOW_NAME-missing", "LOOP_BODY_FLOW_NAME is not configured in "+this.inputFile);
    	}
    	
    	if(executionDO.getEntryFlowDirectory().equals(loopBodyFlowName))
    	{
    		//below execption is to prevent cyclic flow executions
    		throw new SystemException("LOOP_BODY_FLOW_NAME-invalid", "LOOP_BODY_FLOW_NAME is same as the current execution root flow in "+this.inputFile);
    	}
    	
    	ExecutionFlow loopExecutionFlow = executionDO.getEntryExecutionFlows().get(loopBodyFlowName);
    	
    	logger.info("loopListVariablePath:{}", loopListObj);
    	logger.info("loopListVariablePath.getClass():{}", loopListObj.getClass());

    	List<Object> loopList = null;
    	if(loopListObj.getClass().isArray())
    	{
    		Object[] loopArray = (Object[]) loopListObj;
    		loopList = Arrays.asList(loopArray);
    	}
    	else
    		if(loopListObj instanceof Collection)
    		{
    			loopList = new LinkedList<Object>();
    			loopList.addAll((Collection<Object>)loopListObj);
    		}

    	for(int i=0 ; i < loopList.size() ; i++)
    	{
        	LoopIterationDO loopStepDO = new LoopIterationDO(executionDO, loopDO, loopList.get(i), i);
    		loopExecutionFlow.execute(executionDO, loopStepDO);
    	}
	}

}
