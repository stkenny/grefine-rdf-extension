package org.deri.grefine.rdf;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import java.io.IOException;

public class ConstantResourceNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeConstantResourceNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"resource\",\n" + 
				"       \"rdfTypes\" : [\n"
				+ "			{ \"uri\":\"http://u.ri\", \"curie\":\"http://cur.ie\"}"
				+ "		],\n" + 
				"       \"value\" : \"http://my.thing/\"\n" + 
				"     }";
		ConstantResourceNode node = mapper.readValue(json, ConstantResourceNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
