package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class CellLiteralNodeTest {
	@Test
	public void serializeCellLiteralNode() {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(
				new CellLiteralNode("my column", "value.strip()", 
						"http://owl.thing/", "en", true),
				json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoColumnName() {
		String json = "{\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(
				new CellLiteralNode(null, "value.strip()", 
						"http://owl.thing/", "en", true),
				json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoLang() {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"nodeType\" : \"cell-as-literal\",\n" + 
				"       \"valueType\" : \"http://owl.thing/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(
				new CellLiteralNode("my column", "value.strip()", 
						"http://owl.thing/", null, true),
				json);
	}
	
	@Test
	public void serializeCellLiteralNodeNoType() {
		String json = "{\n" + 
				"       \"columnName\" : \"my column\",\n" + 
				"       \"expression\" : \"value.strip()\",\n" + 
				"       \"isRowNumberCell\" : true,\n" + 
				"       \"lang\" : \"en\",\n" + 
				"       \"nodeType\" : \"cell-as-literal\"\n" +  
				"     }";
		TestUtils.isSerializedTo(
				new CellLiteralNode("my column", "value.strip()", 
						null, "en", true),
				json);
	}
}
