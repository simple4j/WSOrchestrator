package org.simple4j.wsorchestrator.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simple4j.wsorchestrator.data.FlowDO;
import org.simple4j.wsorchestrator.model.Flow;

/**
 * This is a utility class with methods to load step and flow properties, deference variables.
 */
public class ConfigLoader
{
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Used to load /flowvariables.properties under flow directories
	 * 
	 * @param variablesFile - input stream to load the variables in properties format
	 * @param globalVariables2 - Existing variables to evaluate bean shell expression 
	 * @return - returns Map of loaded variables
	 * @throws IOException - Any IOException from the system
	 * @throws EvalError - Any BeanShell evaluation errors
	 */
	public static Map<String, Object> loadExecutionOrFlowVariables(File variablesFile,
			Map<String, Object> globalVariables2, String prefix)
	{
		logger.info("Inside loadExecutionOrFlowVariables: {}, {}", globalVariables2, prefix);
		if (prefix == null)
			prefix = "EXECUTION:";
		
		Map<String, Object> jvmVariables2 = getJVMVariables();
		
		Map<String, Object> ret = new HashMap<String, Object>();
		if (jvmVariables2 != null)
		{
			ret.putAll(jvmVariables2);
		}

		if(variablesFile == null || !variablesFile.exists())
		{
			return ret;
		}
		
		Properties loadedVariables = loadProperties(variablesFile);
		HashMap<String, Object> vars = new HashMap<>();
		
		if (jvmVariables2 != null)
		{
			vars.putAll(jvmVariables2);
		}
		ArrayList sortedKeys = new ArrayList(loadedVariables.keySet());
		Collections.sort(sortedKeys);

		for (Iterator iterator = sortedKeys.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();

			logger.debug("processing key:" + key);

			Object eval = null;
			if(loadedVariables.getProperty(key) != null &&
					loadedVariables.getProperty(key).startsWith("MVEL:"))
			{
				eval = MVEL.eval("" + loadedVariables.getProperty(key).replaceFirst("MVEL:", ""), vars);
			}
			logger.debug("MVEL evaluated value:" + eval);
			if (eval != null)
			{
				ret.put(prefix + key, eval);
				vars.put(prefix + key, eval);
				logger.debug("set evaluated:" + key + ":" + eval);
			} else
			{
				ret.put(prefix + key, loadedVariables.getProperty(key));
				vars.put(prefix + key, loadedVariables.getProperty(key));
				logger.debug("set unevaluated:" + key + ":" + loadedVariables.getProperty(key));
			}

		}

		return ret;
	}

	/**
	 * Loads step level properties without MVEL processing
	 * @param stepVariablesFile - this can be step *-input.properties or *-input.properties file
	 * @param flowDO - flow object under which the step is defined
	 * @return Map of loaded properties
	 * @throws IOException - any IOException from the system
	 */
	public static Map<String, Object> loadStepVariables(File stepVariablesFile, FlowDO flowDO)
	{
		logger.info("Inside loadStepVariables: {}, {}", stepVariablesFile, flowDO);
		try
		{
			Properties loadedVariables = loadProperties(stepVariablesFile);
			logger.info("loadedVariables:{}", loadedVariables);
			Map<String, Object> ret = new HashMap<String, Object>();

			ArrayList sortedKeys = new ArrayList(loadedVariables.keySet());
			Collections.sort(sortedKeys);

			for (Iterator iterator = sortedKeys.iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();

				logger.info("processing key:" + key);

				Object eval;
					eval = flowDO.getVariableValue("" + loadedVariables.getProperty(key));
				logger.info("Dereferenced flow property value:" + eval);
				if(eval == null)
				{
					eval=loadedVariables.getProperty(key);
				}
				ret.put("" + key, eval);

			}

			return ret;
		} finally
		{
		}
	}

	private static Map<String, Object> jvmVariables = null;
	private static Map<String, Object> getJVMVariables()
	{
		if(jvmVariables == null)
		{
			Map<String, Object> jvmVariablesLocal = new HashMap<String, Object>();
			Map<String, String> env = System.getenv();
			for (Iterator<Entry<String, String>> iterator = env.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<String, String> entry = iterator.next();
				jvmVariablesLocal.put("ENV:"+entry.getKey(), entry.getValue());
			}
			Properties props = System.getProperties();
			for (Iterator<Entry<Object, Object>> iterator = props.entrySet().iterator(); iterator.hasNext();)
			{
				Entry<Object, Object> entry = iterator.next();
				jvmVariablesLocal.put("ENV:"+entry.getKey(), entry.getValue());
			}
			jvmVariables = jvmVariablesLocal;
		}
		return jvmVariables;
	}

	private static ConcurrentHashMap<File, Properties> loadedPropertiesCache = new ConcurrentHashMap<File, Properties>();
	public static Properties loadProperties(File variablesFile)
	{
		Properties ret = new Properties();
		if(loadedPropertiesCache.containsKey(variablesFile))
		{
			ret.putAll(loadedPropertiesCache.get(variablesFile));
			return ret;
		}
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(variablesFile);
			Properties loadedVariables = new Properties();
			loadedVariables.load(fis);
			loadedPropertiesCache.put(variablesFile, loadedVariables);
			ret.putAll(loadedPropertiesCache.get(variablesFile));
			return ret;
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		} finally
		{
    		if(fis != null)
    		{
				try
				{
					fis.close();
				} catch (IOException e)
				{
					logger.warn("Error while closing cutom variable stream :{}", variablesFile, e);
				}
    		}
		}
	}

	private static HashMap<String, List<File>> filePath2ChildrenDirectories = new HashMap<>();
	public static List<File> getChildrenDirectories(File parentDir)
	{
		try
		{
			String key = parentDir.getCanonicalPath();
			if(filePath2ChildrenDirectories.containsKey(key))
				return filePath2ChildrenDirectories.get(key);
			File[] childrenDirs = parentDir.listFiles(new FileFilter() {
	
				@Override
				public boolean accept(File pathname)
				{
					return pathname.isDirectory() && pathname.exists();
				}});
	      
			List<File> childrenDirsList = Arrays.asList(childrenDirs);
			filePath2ChildrenDirectories.put(key, childrenDirsList);
			return childrenDirsList;
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
