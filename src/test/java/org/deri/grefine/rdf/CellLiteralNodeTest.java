package org.deri.grefine.rdf;

import java.io.IOException;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CellLiteralNodeTest {
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeCellLiteralNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		CellLiteralNode node = mapper.readValue(json, CellLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoColumnName() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		CellLiteralNode node = mapper.readValue(json, CellLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoLang() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		CellLiteralNode node = mapper.readValue(json, CellLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoType() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\"\n" +  
				"     }";
		CellLiteralNode node = mapper.readValue(json, CellLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
