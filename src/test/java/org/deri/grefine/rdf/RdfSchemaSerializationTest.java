package org.deri.grefine.rdf;

import org.deri.grefine.util.TestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RdfSchemaSerializationTest {

	private RdfSchema schema;
	private String json;
	private ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	public void init() throws Exception{
		json = "{\"baseUri\":\"http://data.bis.gov.uk/data/organogram/2010-08-26/\",\"prefixes\":[],\"rootNodes\":[{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"'http://reference.data.gov.uk/id/department/bis/post/' + value\",\"columnName\":\"Post unique reference\",\"rdfTypes\":[{\"uri\":\"http://reference.data.gov.uk/def/central-government/CivilServicePost\",\"curie\":\"gov:CivilServicePost\"}],\"links\":[{\"uri\":\"http://reference.data.gov.uk/def/central-government/heldBy\",\"curie\":\"http://reference.data.gov.uk/def/central-government/heldBy\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":true,\"expression\":\"('#person' + value).urlify()\",\"rdfTypes\":[{\"uri\":\"http://xmlns.com/foaf/0.1/Person\",\"curie\":\"foaf:Person\"}],\"links\":[{\"uri\":\"http://xmlns.com/foaf/0.1/name\",\"curie\":\"foaf:name\",\"target\":{\"nodeType\":\"cell-as-literal\",\"expression\":\"value\",\"isRowNumberCell\":false,\"columnName\":\"Name\"}},{\"uri\":\"http://xmlns.com/foaf/0.1/mbox\",\"curie\":\"foaf:mbox\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"'mailto:' + value\",\"columnName\":\"Contact e-mail\",\"rdfTypes\":[],\"links\":[]}}]}},{\"uri\":\"http://www.w3.org/2000/01/rdf-schema#label\",\"curie\":\"rdfs:label\",\"target\":{\"nodeType\":\"cell-as-literal\",\"expression\":\"value\",\"isRowNumberCell\":false,\"lang\":\"en\",\"columnName\":\"Job Title\"}},{\"uri\":\"http://reference.data.gov.uk/def/central-government/postIn\",\"curie\":\"http://reference.data.gov.uk/def/central-government/postIn\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"value.urlify()\",\"columnName\":\"Unit\",\"rdfTypes\":[],\"links\":[{\"uri\":\"http://reference.data.gov.uk/def/central-government/hasPost\",\"curie\":\"http://reference.data.gov.uk/def/central-government/hasPost\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"'http://reference.data.gov.uk/id/department/bis/post/' + value\",\"columnName\":\"Post unique reference\",\"rdfTypes\":[],\"links\":[]}}]}}]}]}";
		schema = mapper.readValue(json, RdfSchema.class);
	}
	
	@Test(groups={"rdf-schema-test"})
	public void testSerialization() {
		TestUtils.isSerializedTo(schema, json);
	}
}
