package org.deri.grefine.rdf;

import org.deri.grefine.rdf.ResourceNode.RdfType;
import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class ConstantResourceNodeTest {
	@Test
	public void serializeConstantResourceNode() {
		ConstantResourceNode node = new ConstantResourceNode("http://my.thing/");
		node.addType(new RdfType("http://u.ri", "http://cur.ie"));
		String json = "{\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"resource\",\n" + 
				"       \"rdfTypes\" : [\n"
				+ "			{ \"uri\":\"http://u.ri\", \"curie\":\"http://cur.ie\"}"
				+ "		],\n" + 
				"       \"value\" : \"http://my.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
}
