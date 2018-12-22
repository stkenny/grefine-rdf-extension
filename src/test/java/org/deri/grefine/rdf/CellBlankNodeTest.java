package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class CellBlankNodeTest {
	@Test
	public void serializeCellBlankNode() {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		CellBlankNode node = new CellBlankNode("my column", "value", false);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeCellBlankNodeNoColumn() {
		String json = "{\n" +  
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		TestUtils.isSerializedTo(new CellBlankNode(null, "value", false), json);
	}
}
