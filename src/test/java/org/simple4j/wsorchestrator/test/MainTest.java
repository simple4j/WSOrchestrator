package org.simple4j.wsorchestrator.test;


import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simple4j.wsorchestrator.Orchestrator;
import org.simple4j.wsorchestrator.data.FlowDO;
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
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		wm1 = new WireMockServer(WireMockConfiguration.options().bindAddress("localhost").port(2001).withRootDirectory(MainTest.class.getResource("/wiremock").getPath()));
		wm1.start();
		flowsDir = new File(MainTest.class.getResource("/flowsDir").getPath());
		ac = new ClassPathXmlApplicationContext("/connectors/wiremock/appContext.xml");
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
	@Test
	public void testSimpleFlow()
	{
		Orchestrator orch = new Orchestrator(flowsDir, ac);
		FlowDO response = orch.execute("simpleFlow");
		System.out.println("testSimpleFlow response:"+ response);
		System.out.println("./010-step-ws/callerId="+response.getVariableValue("./010-step-ws/callerId"));
//		Assert.assertTrue("Failed testcases are :" + tcPaths, success);
	}
}
