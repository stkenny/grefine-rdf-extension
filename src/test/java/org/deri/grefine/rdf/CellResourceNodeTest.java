package org.deri.grefine.rdf;

import java.io.IOException;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CellResourceNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeCellResourceNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value\",\n" + 
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-resource\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		CellResourceNode node = mapper.readValue(json, CellResourceNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
