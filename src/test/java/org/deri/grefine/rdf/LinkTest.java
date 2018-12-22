package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.Test;

public class LinkTest {
	@Test
	public void serializeLinkNoTarget() {
		Link link = new Link("http://my.uri/", "http://my.curie/", null);
		String json = "{\n" + 
				"       \"curie\" : \"http://my.curie/\",\n" + 
				"       \"uri\" : \"http://my.uri/\"\n" + 
				"     }";
		TestUtils.isSerializedTo(link, json);
	}
}
