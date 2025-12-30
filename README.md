# WSOrchestrator

Simple4j WSOrchestrator is a configurable Web Service orchestration framework that can be used to for all types of services APIs (XML, JSON, SOAP, REST) over both http and https protocols.

The configuration (and the internal structure) is organized as hierarchy of execution flows and execution steps.
As the name suggests, execution step is a step that can be executed. This is typically a web service step which will invoke a web service API. The execution step is extendible to other types of steps. A step is represented with a pair of as pair of <step name>-input.properties and <step name>-output.properties. The input file has the variables needed for the execution of the step and output file has the variables that can be retrieved from the response of the web service.
The execution flow is a collection of execution steps and/or execution sub-flows. Execution flows and sub-flows are organized as directories and sub-directories.

Peer steps in a flow are executed in sequence and the peer flows are executed in parallel across multiple threads.
	
A flow can have series of steps and sub-flows. They are executed in the natural ordering of their names.

<ul>
For example, based on natural ordering,
<ul>
	if there are sequence of steps,
	followed by sequence of sub-flows,
	followed by another set of steps,
<ul>
		then the first set of steps will be executed in sequence,
		then the sub-flows will be executed in parallel,
		then wait for all the sub-flows to finish
		and execute the second set of steps in sequence.
</ul>
</ul>
</ul>

The variable from a step X can be used as an input to another step after step X. The variable from a step X can also be used as an input to another step in one of the sub-flows after the step X.

Once the configuration is loaded and the internal model hierarchy initialized based on the configuration at the startup, a flow can be executed by the client application which would create an instance of ExecutionDO representing an execution and hierarchical instances of ExecutionFlowDO/ExecutionStepDO.

There is a sample configuration using mock web service, under the maven standard src/test/resources path.
The root of directory for configuration is "flowsRootDirecory" which has the top level set of executable flows.
"connectors" directory has the spring application context xmls to create and fetch instance of org.simple4j.wsclient.caller.ICaller for the step execution.
"wiremock" directory has the configuration for the mock services for testing.

