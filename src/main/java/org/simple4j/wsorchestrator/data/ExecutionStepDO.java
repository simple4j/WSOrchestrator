package org.simple4j.wsorchestrator.data;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.mvel2.MVEL;
import org.simple4j.wsclient.util.CollectionsPathRetreiver;
import org.simple4j.wsorchestrator.exception.SystemException;
import org.simple4j.wsorchestrator.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will hold the data of an execution step.
 */
public class ExecutionStepDO extends ValueRetriever
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ExecutionDO executionDO = null;
	private ExecutionFlowDO parent = null;
	private File inputFile = null;
	private File outputFile = null;
	private String name = null;
	private String shortName = null;
	
	public ExecutionStepDO(ExecutionDO executionDO, ExecutionFlowDO parent, File inputFile, File outputFile)
	{
		this.executionDO = executionDO;
		this.parent = parent;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.variables = ConfigLoader.loadStepVariables(inputFile, parent);

		String stepAbsolutePath = inputFile.getAbsolutePath();
		this.name = stepAbsolutePath.substring(this.executionDO.getFlowsRootDirectory().getAbsolutePath().length(),stepAbsolutePath.length()-"-input.properties".length());
		String inputFileName = inputFile.getName();
		logger.info("inputFileName {}", inputFileName);
		this.shortName = inputFileName.substring(0,inputFileName.length()-"-input.properties".length());
		logger.info("shortName {}", this.shortName);
		this.parent.variables.put(shortName, this);
	}
	
	@Override
	public Object getVariableValue(String variableName)
	{
		logger.debug("getvariableValue {}", variableName);
		Object ret = null;
		if(variableName.contains("/"))
		{
			if(variableName.startsWith("../"))
				return this.parent.getVariableValue(variableName);
			
			if(variableName.startsWith("./"))
			{
				variableName = variableName.substring(2);
				return this.parent.getVariableValue(variableName);
			}
			
		}
		ret = super.getVariableValue(variableName);
		if(ret != null)
			return ret;
		return this.executionDO.getVariableValue(variableName);
	}
	
	public boolean canExecute()
	{
		boolean ret = true;
		if(this.variables.containsKey("EXECUTE_IF"))
		{
			String executeIfExpression = ""+this.variables.get("EXECUTE_IF");

            Object executeIfExpressionResult;
			try
			{
				logger.info("executeIfExpression : {}", executeIfExpression);
				executeIfExpressionResult = MVEL.eval(executeIfExpression, this.variables);
				logger.info("executeIfExpressionResult : {}", executeIfExpressionResult);
			} catch (Throwable e)
    		{
				logger.error("Error while evaluating EXECUTE_IF: {} in step: {}", executeIfExpression, this.name, e);
	            throw new RuntimeException(e);
			}
            if(executeIfExpressionResult instanceof Boolean)
            {
                if(!((Boolean)executeIfExpressionResult))
                {
                   ret = false;
                   this.variables.put("EXECUTE_IF", false);
                }
                else
                {
                    this.variables.put("EXECUTE_IF", true);
                }
            }
            else
            {
                logger.info("Execute if expression "+executeIfExpression+" return non-boolean value "+ executeIfExpressionResult + " of type "+executeIfExpressionResult.getClass());
                ret = false;
                this.variables.put("EXECUTE_IF", false);
            }
		}
		return ret;
	}

	public void processStepExecutionResponse(Map<String, Object> response)
	{
		this.variables.putAll(response);
		if(this.outputFile != null)
		{
			Properties outputProperties = ConfigLoader.loadProperties(outputFile);
			String assertExpression = null;
			if(outputProperties.containsKey("ASSERT"))
			{
				assertExpression = outputProperties.getProperty("ASSERT");
				outputProperties.remove("ASSERT");
			}
	        for (Entry<Object, Object> entry : outputProperties.entrySet())
	        {
	        	CollectionsPathRetreiver cpr = new CollectionsPathRetreiver();
	        	logger.info("fetching property|{}| from |{}|",""+entry.getValue(), response);
	            List nestedProperty = cpr.getNestedProperty(response, ""+entry.getValue());
	            Object value = nestedProperty;
	            if(nestedProperty.size() == 0)
	            {
	            	continue;
	            }
	            if(nestedProperty.size() == 1)
	            {
	            	value = nestedProperty.get(0);
	            }
            	logger.info("setting key to intrepreter: {} value: {}", entry.getKey(), value);
				this.variables.put(""+entry.getKey(), ""+value);
	        }
	        if(assertExpression != null)
	        {
	            Object assertExpressionResult;
				try
				{
					logger.info("assertExpression : {}", assertExpression);
					assertExpressionResult = MVEL.eval(assertExpression, this.variables);
					logger.info("assertExpressionResult : {}", assertExpressionResult);
				} catch (Throwable e)
	    		{
					logger.error("Error while evaluating ASSERT: {} in step: {}", assertExpression, this.name, e);
		            throw new RuntimeException(e);
				}
	            if(assertExpressionResult instanceof Boolean)
	            {
	                if(!((Boolean)assertExpressionResult))
	                {
	                    logger.info("FAILURE: step "+ this.name +" for assertion "+assertExpression);
	                    logger.info("Step variables are "+outputProperties);
	    	            throw new SystemException("ASSERTION_FAILURE", "Step "+ this.name +" for assertion "+assertExpression);
	                }
	            }
	            else
	            {
	                logger.info("FAILURE: Assertion expression "+assertExpression+" return non-boolean value "+ assertExpressionResult + " of type "+assertExpressionResult.getClass());
	                throw new SystemException("ASSERTION_NON_BOOLEAN", "Assertion expression "+assertExpression+" return non-boolean value "+ assertExpressionResult + " of type "+assertExpressionResult.getClass());
	            }
	        }
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
//		if(this.parent == null)
//			builder.append(" [executionDO=").append(executionDO);
		builder.append("[inputFile=").append(inputFile)
		.append(", outputFile=").append(outputFile).append(", name=")
		.append(name).append(", shortName=").append(shortName).append(", variables=").append(variables)
		.append("]");
		return builder.toString();
	}
}
