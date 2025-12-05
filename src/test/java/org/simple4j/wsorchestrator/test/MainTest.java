package org.simple4j.wsorchestrator.test;


import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simple4j.wsorchestrator.Orchestrator;
import org.simple4j.wsorchestrator.data.ExecutionFlowDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.collect.Lists;

public class MainTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	static WireMockServer wm1 = null;
	static File flowsDir = null;
	static ApplicationContext ac = null;
	static Orchestrator orch = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		wm1 = new WireMockServer(WireMockConfiguration.options().bindAddress("localhost").port(2001).withRootDirectory(MainTest.class.getResource("/wiremock").getPath()));
		wm1.start();
		flowsDir = new File(MainTest.class.getResource("/flowsRootDirecory").getPath());
		ac = new ClassPathXmlApplicationContext("/connectors/wiremock/appContext.xml");
		orch = new Orchestrator(flowsDir, ac);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		if(wm1 != null)
			wm1.shutdownServer();
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	/*
	 * 1. simple flow with at least 3 steps with cross referencing variables
	 * 2. one level sub-flows with cross referencing variables
	 * 3. two level sub-flows with cross referencing variables
	 * 4. flow with steps, sub-flows, steps and sub-flows
	 */
	
	/**
	 * This test case is for a simple one level flow with steps that have inter-step referencing variables.
	 * 
	 */
	@Test
	public void testSimpleFlow()
	{
		Map<String, Object> executionParameters = new HashMap<String, Object>();
		UUID uuid = UUID.randomUUID();
		
		String stringUUID = uuid.toString();
		executionParameters.put("someExecutionParam", stringUUID);
		ExecutionFlowDO response = orch.execute("simpleFlow", executionParameters );
//		System.out.println("testSimpleFlow response:"+ response);

		Object respCallerId = response.getVariableValue("./010-step-ws/callerId");
		Object flowRandNum3Digit = response.getVariableValue("./FLOW:RANDOM_NUM_3DIGIT");
		Object respHeader2 = response.getVariableValue("./010-step-ws/header2");
		
		System.out.println("./010-step-ws/callerId="+respCallerId);
		System.out.println("./FLOW:RANDOM_NUM_3DIGIT="+flowRandNum3Digit);
		
		Assert.assertEquals( respCallerId,flowRandNum3Digit );
		Assert.assertEquals("executionParameters check failure", respHeader2, stringUUID );
	}

	/**
	 * This test case is for 1 level of multiple sub-flows and make sure the parallel execution done between sub-flows
	 */
	@Test
	public void testSubLevelFlow()
	{
		ExecutionFlowDO response = orch.execute("sublevelFlow", null);
//		System.out.println("testSubLevelFlow response:"+ response);
		Object respCallerId = response.getVariableValue("./02-subflow/020-step-ws/callerId");
		Object flowRandNum3Digit = response.getVariableValue("./02-subflow/FLOW:RANDOM_NUM_3DIGIT");
		
		System.out.println("./02-subflow/020-step-ws/callerId="+respCallerId);
		System.out.println("./02-subflow/FLOW:RANDOM_NUM_3DIGIT="+flowRandNum3Digit);
		
		Assert.assertEquals( respCallerId,flowRandNum3Digit );
	}

	/**
	 * This test case is for 2 levels of multiple sub-flows and some flows not having flowvariables.properties.
	 * This also has variable referencing of flow variable from a parent flow.
	 */
	@Test
	public void test2SubLevelFlow()
	{
		ExecutionFlowDO response = orch.execute("2sublevelFlow", null);
//		System.out.println("testSubLevelFlow response:"+ response);
		Object respCallerId = response.getVariableValue("./02-subflow/21-subflow/010-step-ws/callerId");
		Object flowRandNum3Digit = response.getVariableValue("./02-subflow/21-subflow/FLOW:RANDOM_NUM_3DIGIT");
		
		System.out.println("./02-subflow/21-subflow/010-step-ws/callerId="+respCallerId);
		System.out.println("./02-subflow/21-subflow/FLOW:RANDOM_NUM_3DIGIT="+flowRandNum3Digit);
		
		Assert.assertEquals( respCallerId,flowRandNum3Digit );
	}

	/**
	 * This test case is to test combination of steps and flows to make sure the execution switches
	 * between serial and parallel execution.
	 * 
	 */
	@Test
	public void testComplexFlow()
	{
		ExecutionFlowDO response = orch.execute("complexFlow", null);
//		System.out.println("testSubLevelFlow response:"+ response);
		Object respCallerId = response.getVariableValue("./40-subflow/21-subflow/010-step-ws/callerId");
		Object flowRandNum3Digit = response.getVariableValue("./40-subflow/21-subflow/FLOW:RANDOM_NUM_3DIGIT");
		
		System.out.println("./02-subflow/21-subflow/010-step-ws/callerId="+respCallerId);
		System.out.println("./02-subflow/21-subflow/FLOW:RANDOM_NUM_3DIGIT="+flowRandNum3Digit);
		
		Assert.assertEquals( respCallerId,flowRandNum3Digit );
	}
}
