package org.simple4j.wsorchestrator.model;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.simple4j.wsclient.caller.CallerFactory;
import org.simple4j.wsclient.caller.ICaller;
import org.simple4j.wsclient.exception.SystemException;
import org.simple4j.wsorchestrator.data.ExecutionDO;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.simple4j.wsorchestrator.data.ExecutionStepDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of ExecutionStep that encapsulates the logic to execute a Web Service execution step that makes the Web Service call.
 */
public class WSExecutionStep extends ExecutionStep
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public WSExecutionStep(File inputFile)
	{
		super(inputFile);
	}

	@Override
	public void execute(ExecutionDO executionDO, ExecutionFlowDO parent)
	{
		// 1. get caller factory bean id or caller bean id from input file
		// 2. get caller from connectorsAppliactionContext from executionDO
		// 3. call the caller with the input variables
		// 4. call super to process response from the caller

		ExecutionStepDO executionStepDO = new ExecutionStepDO(executionDO, parent, this.inputFile, this.outputFile);
		
		ICaller caller = null;
    	Object callerBeanIdObj = executionStepDO.getVariableValue("callerBeanId");
    	if(callerBeanIdObj != null)
    	{
    		String callerBeanId = (String) callerBeanIdObj;
            caller = executionDO.getConnectorsApplicationContext().getBean(callerBeanId, ICaller.class);;
    	}
    	else
    	{
        	Object callerFactoryBeanIdObj = executionStepDO.getVariableValue("callerFactoryBeanId");
        	if(callerFactoryBeanIdObj != null)
        	{
        		String callerFactoryBeanId = (String) callerFactoryBeanIdObj;
                caller = executionDO.getConnectorsApplicationContext().getBean(callerFactoryBeanId, CallerFactory.class).getCaller();
        	}
        	else
        	{
        		throw new SystemException("callerBeanId-callerFactoryBeanId-missing", "Both callerBeanId and callerFactoryBeanId is missing in the file:"+this.inputFile);
        	}
    	}
        logger.debug("Calling service");

		
		Map<String, Object> response = caller.call(executionStepDO.getVariables());
		executionStepDO.processStepExecutionResponse(response);
		
	}

}
