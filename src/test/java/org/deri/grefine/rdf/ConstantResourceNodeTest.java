package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class ConstantResourceNodeTest {
	@Test
	public void serializeConstantResourceNode() {
		ConstantResourceNode node = new ConstantResourceNode("http://my.thing/");
		String json = "{\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"resource\",\n" + 
				"       \"rdfTypes\" : [ ],\n" + 
				"       \"value\" : \"http://my.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
}
