package org.simple4j.wsorchestrator.data;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simple4j.wsorchestrator.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a super class of data objects of the library that can hold the runtime variables as key/value pairs
 */
public class ValueRetriever
{

	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * variables map holding key/values pairs
	 */
	protected Map<String, Object> variables = new ConcurrentHashMap<String, Object>();

	/**
	 * This returns a copy of the variables map. This only copies the name/value object references and does not do a deep copy of the values.
	 * The caller of this method should not change the value of any attributes of the value objects. Otherwise, the execution will give unpredictable results.
	 * 
	 * @return
	 */
	public Map<String, Object> getVariables()
	{
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.putAll(this.variables);
		return ret ;
	}

	/**
	 * This method returns the variable value for a given variableName (aka key)
	 * The variableName can navigate across different levels based on / ./ ../ notations like file system paths
	 * 
	 * @param variableName
	 * @return
	 */
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
				throw new SystemException("CHILD_TYPE_INVALID", "Child is not of type ValueRetriever");
			}
		}
		
		return null;
	}

}
