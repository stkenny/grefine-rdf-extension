package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class ConstantBlankNodeTest {
	@Test
	public void serializeConstantBlankNode() {
		ConstantBlankNode node = new ConstantBlankNode(383);
		String json = "{\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
}
