package org.deri.grefine.rdf;

import java.io.IOException;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConstantBlankNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeConstantBlankNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		ConstantBlankNode node = mapper.readValue(json, ConstantBlankNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
