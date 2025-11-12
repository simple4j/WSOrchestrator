# WSOrchestrator

Simple4j WSOrchestrator is a configurable Web Service orchestration framework that can be used to for all types of services APIs (XML, JSON, SOAP, REST) over both http and https protocols.
	
It supports hierarchy of flows and steps with wiring of request response objects using templated variable replacement.
Peer steps in a flow are executed in sequence and the peer flows are executed in parallel across multiple threads.
	
A flow can have series of steps and sub-flows. They are executed in the natural ordering of their names.

Based on natural ordering,
	if there are sequence of steps,
	followed by sequence of sub-flows,
	followed by another set of steps,
		then the first set of steps will be executed in sequence,
		then the sub-flows will be executed in parallel,
		then wait for all the sub-flows to finish
		and execute the second set of steps in sequence.

Flows and sub-flows are organized as directories and sub-directories.
Steps are organized as pair of <step name>-input.properties and <step name>-output.properties

There is a sample configuration using mock web service, under the maven standard src/test/resources path.
The root of directory for configuration is flowsDir which has the top level set of executable flows.
connectors directory has the spring application context xmls to create and fetch instance of org.simple4j.wsclient.caller.ICaller for the step execution.
wiremock directory has the configuration for the mock services for testing.


