package org.deri.grefine.rdf;

import java.io.IOException;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CellBlankNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeCellBlankNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		CellBlankNode node = mapper.readValue(json, CellBlankNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeCellBlankNodeNoColumn() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" +  
				"       \"isRowNumberCell\" : false,\n" + 
				"       \"links\" : [ ],\n" + 
				"       \"nodeType\" : \"cell-as-blank\",\n" + 
				"       \"rdfTypes\" : [ ]\n" + 
				"     }";
		CellBlankNode node = mapper.readValue(json, CellBlankNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
