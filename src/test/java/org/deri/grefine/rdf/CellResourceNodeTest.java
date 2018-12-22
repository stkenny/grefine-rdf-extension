package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class CellResourceNodeTest {
	@Test
	public void serializeCellResourceNode() {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value\",\n" + 
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-resource\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		TestUtils.isSerializedTo(
				new CellResourceNode("my column", "value", false),
				json);
	}
}
