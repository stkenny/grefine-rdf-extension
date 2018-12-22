package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class ConstantLiteralNodeTest {
	@Test
	public void serializeConstantLiteralNode() {
		ConstantLiteralNode node = new ConstantLiteralNode("my value", "http://owl.thing/", "en");
		String json = "{\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeConstantLiteralNodeNoType() {
		ConstantLiteralNode node = new ConstantLiteralNode("my value", null, "en");
		String json = "{\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\"\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
	
	@Test
	public void serializeConstantLiteralNodeNoLang() {
		ConstantLiteralNode node = new ConstantLiteralNode("my value", "http://owl.thing/", null);
		String json = "{\n" + 
				"       \"nodeType\" : \"literal\",\n" + 
				"       \"value\" : \"my value\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(node, json);
	}
}
