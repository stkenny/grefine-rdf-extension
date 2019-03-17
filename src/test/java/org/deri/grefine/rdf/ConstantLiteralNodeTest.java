package org.deri.grefine.rdf;

import java.io.IOException;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConstantLiteralNodeTest {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void serializeConstantLiteralNode() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		ConstantLiteralNode node = mapper.readValue(json, ConstantLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeConstantLiteralNodeNoType() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\"\n" + 
				"     }";
		ConstantLiteralNode node = mapper.readValue(json, ConstantLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeConstantLiteralNodeNoLang() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		ConstantLiteralNode node = mapper.readValue(json, ConstantLiteralNode.class);
		TestUtils.isSerializedTo(node, json);
	}
}
