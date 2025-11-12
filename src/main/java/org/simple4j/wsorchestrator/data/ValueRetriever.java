package org.simple4j.wsorchestrator.data;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueRetriever
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Map<String, Object> variables = null;
	
	public Map<String, Object> getVariables()
	{
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.putAll(this.variables);
		return ret ;
	}

	public Object getVariableValue(String variableName)
	{
		if(this.variables == null)
			return null;
		
		logger.info("Entering getvariableValue {}", variableName);
		
		Object ret = this.variables.get(variableName);
		if(ret != null)
			return ret;
		
		if(variableName.contains("/"))
		{
			//Could be nested lookup
			if(variableName.startsWith("./"))
				variableName = variableName.substring(2);
			
			if(variableName.startsWith("/"))
				variableName = variableName.substring(1);
			
			String childKey = variableName;
			int firstSlashIndex = childKey.indexOf("/");
			String subChildKey = null;
			if(firstSlashIndex > 0)
			{
				childKey = childKey.substring(0, firstSlashIndex);
				subChildKey = variableName.substring(firstSlashIndex);
			}
			
			Object childDO = this.variables.get(childKey);
			logger.debug("childDO {}", childDO);
			if(childDO == null)
				return null;
			
			if(subChildKey == null)
				return childDO;
			
			if (childDO instanceof ValueRetriever)
			{
				return ((ValueRetriever) childDO).getVariableValue(subChildKey);
			}
			else
			{
				throw new RuntimeException("Child is not of type ValueRetriever");
			}
		}
		
		return null;
	}

}
