package org.deri.grefine.rdf.exporters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.deri.grefine.rdf.Node;
import org.deri.grefine.rdf.vocab.Vocabulary;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RepositoryUtil;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.refine.browsing.Engine;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.grel.ControlFunctionRegistry;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.expr.RdfBinder;
import org.deri.grefine.rdf.expr.functions.strings.Urlify;
import com.google.refine.util.ParsingUtilities;

import java.io.StringWriter;
import java.util.List;

public class RdfExporterMultiRootNodesTest {
	
	Project project;
	Engine engine;
	RdfExporter exporter;
	Repository model;
	
	Repository expected;
	RdfSchema schema;

	@BeforeClass
	public void init() throws Exception{
		expected = buildExpectedModel();
		ApplicationContext ctxt = new ApplicationContext();
		schema = getRdfSchema();
		project = RdfExporterFacultyDataTest.buildTheSampleProject(schema);
		engine = new Engine(project);
		exporter = new RdfExporter(ctxt,RDFFormat.RDFXML);
		ControlFunctionRegistry.registerFunction("urlify", new Urlify());
		ExpressionUtils.registerBinder(new RdfBinder(ctxt));

		StringWriter sw = new StringWriter();
		RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, sw);

		RdfExporter.RdfRowVisitor visitor = new RdfExporter.RdfRowVisitor(schema, w) {
			final int limit = 10;
			int _count;
			@Override
			public boolean visit(Project project, int rowIndex , Row row) {
				if(_count >= limit){
					return true;
				}
				for(Node root:roots){
					root.createNode(baseUri, factory, con, project, row, rowIndex, blanks);
				}
				_count +=1;

				try {
					List<Resource> resourceList = con.getContextIDs().asList();
					Resource[] resources = resourceList.toArray(new Resource[resourceList.size()]);

					// Export statements
					RepositoryResult<Statement> stIter =
							con.getStatements(null, null, null, false, resources);

					try {
						while (stIter.hasNext()) {
							this.writer.handleStatement(stIter.next());
						}
					} finally {
						stIter.close();
					}
				} catch (RepositoryException e) {
					e.printStackTrace();
					return true;
				} catch (RDFHandlerException e) {
					e.printStackTrace();
					return true;
				}

				return false;
			}
		};

		for(Vocabulary v:schema.getPrefixesMap().values()){
			w.handleNamespace(v.getName(), v.getUri());
		}

		model = exporter.buildModel(project, engine, visitor);
		
		assertEquals(project.rows.size(),3);
		assertEquals(project.columnModel.getColumnIndexByName("Advisor"),5);
		
		Row row = project.rows.get(0);
		assertEquals(row.cells.get(5).value,"");
		row = project.rows.get(1);
		assertEquals(row.cells.get(5).value,"Tim Finin");
		row = project.rows.get(2);
		assertEquals(row.cells.get(5).value,"Anupam Joshi");
	}
	
	@Test(groups={"rdf-schema-test"})
	public void testModel()throws Exception{
		assertTrue(RepositoryUtil.equals(expected, model));
	} 
	
	RdfSchema getRdfSchema()throws Exception{
		String json = "{\"baseUri\":\"http://lab.linkeddata.deri.ie/test#\",\"rootNodes\":[{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"value.urlify()\",\"columnName\":\"Name\",\"rdfTypes\":[{\"uri\":\"http://xmlns.com/foaf/0.1/Person\",\"curie\":\"foaf:Person\"}],\"links\":[{\"uri\":\"http://xmlns.com/foaf/0.1/name\",\"curie\":\"foaf:name\",\"target\":{\"nodeType\":\"cell-as-literal\",\"expression\":\"value\",\"isRowNumberCell\":false,\"columnName\":\"Name\"}},{\"uri\":\"http://xmlns.com/foaf/0.1/mbox\",\"curie\":\"foaf:mbox\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"'mailto:' + value\",\"rdfTypes\":[],\"columnName\":\"Email\",\"links\":[]}},{\"uri\":\"officeNumber\",\"curie\":\":officeNumber\",\"target\":{\"nodeType\":\"cell-as-literal\",\"expression\":\"value\",\"isRowNumberCell\":false,\"valueType\":\"http://www.w3.org/2001/XMLSchema#int\",\"columnName\":\"Office\"}},{\"uri\":\"http://xmlns.com/foaf/0.1/member\",\"curie\":\"foaf:member\",\"target\":{\"nodeType\":\"resource\",\"value\":\"http://example.org/UMBC\",\"rdfTypes\":[{\"uri\":\"http://xmlns.com/foaf/0.1/Organization\",\"curie\":\"foaf:Organization\"}],\"links\":[{\"uri\":\"http://www.w3.org/2000/01/rdf-schema#label\",\"curie\":\"rdfs:label\",\"target\":{\"nodeType\":\"literal\",\"value\":\"University of Maryland Baltimore County\",\"lang\":\"en\"}}]}},{\"uri\":\"advisor\",\"curie\":\":advisor\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"value.urlify()\",\"columnName\":\"Advisor\",\"rdfTypes\":[{\"uri\":\"Advisor\",\"curie\":\":Advisor\"}],\"links\":[{\"uri\":\"http://xmlns.com/foaf/0.1/name\",\"curie\":\"foaf:name\",\"target\":{\"nodeType\":\"cell-as-literal\",\"expression\":\"value\",\"isRowNumberCell\":false,\"columnName\":\"Advisor\"}}]}}]},{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"value.urlify()\",\"columnName\":\"Advisor\",\"rdfTypes\":[{\"uri\":\"Advisor\",\"curie\":\"Advisor\"}],\"links\":[{\"uri\":\"advise\",	\"curie\":\"advise\",\"target\":{\"nodeType\":\"cell-as-resource\",\"isRowNumberCell\":false,\"expression\":\"value.urlify()\",\"columnName\":\"Name\",\"rdfTypes\":[],\"links\":[{\"uri\":\"college\",\"curie\":\"college\",\"target\":{\"nodeType\":\"resource\",	\"value\":\"http://example.org/ScienceCollege\", \"rdfTypes\":[], \"links\":[]}}]}}]}]}";
		JSONObject o = ParsingUtilities.evaluateJsonStringToObject(json);
		return RdfSchema.reconstruct(o);
	}

	Repository buildExpectedModel() throws Exception{
		//see test2.xlsx and rdfschema2.png

		Repository model = new SailRepository(new MemoryStore());
		model.initialize();
		
		RepositoryConnection con = null;
		try{
			con = model.getConnection();
			ValueFactory vf = con.getValueFactory();
			
			//create persons 
			URI person1 = addFoafPerson(vf, con, "http://lab.linkeddata.deri.ie/test#tim-finin", "Tim Finin", "finin@umbc.edu");
			URI person2 = addFoafPerson(vf, con, "http://lab.linkeddata.deri.ie/test#lushan-han", "Lushan Han", "lushan@umbc.edu");
			URI person3 = addFoafPerson(vf, con, "http://lab.linkeddata.deri.ie/test#wenjia-li", "Wenjia Li", "wenjia@umbc.edu");
		
			//office numbers
			URI officeNumberProp = vf.createURI("http://lab.linkeddata.deri.ie/test#officeNumber");
			con.add(vf.createStatement(person1, officeNumberProp, vf.createLiteral(329)));
			con.add(vf.createStatement(person2, officeNumberProp, vf.createLiteral(377)));
			con.add(vf.createStatement(person3, officeNumberProp, vf.createLiteral(377)));
		
			//advisor
			URI advisor = vf.createURI("http://lab.linkeddata.deri.ie/test#anupam-joshi");
			con.add(vf.createStatement(advisor, vf.createURI("http://xmlns.com/foaf/0.1/name"), vf.createLiteral("Anupam Joshi")));
			URI advisorType = vf.createURI("http://lab.linkeddata.deri.ie/test#Advisor");
			con.add(vf.createStatement(advisor, RDF.TYPE, advisorType));
			con.add(vf.createStatement(person1, RDF.TYPE, advisorType));
		
			//university
			URI university = vf.createURI("http://example.org/UMBC");
			con.add(vf.createStatement(university,RDF.TYPE, vf.createURI("http://xmlns.com/foaf/0.1/Organization")));
			con.add(vf.createStatement(university,RDFS.LABEL,vf.createLiteral("University of Maryland Baltimore County","en")));
		
			URI memberProp = vf.createURI("http://xmlns.com/foaf/0.1/member");
			con.add(vf.createStatement(person1,memberProp,university));
			con.add(vf.createStatement(person2,memberProp,university));
			con.add(vf.createStatement(person3,memberProp,university));
		
			//add advisor
			URI advisorProp = vf.createURI("http://lab.linkeddata.deri.ie/test#advisor");
			con.add(vf.createStatement(person2,advisorProp,person1));
			con.add(vf.createStatement(person3,advisorProp,advisor));
			
			//second root node data
			URI adviseProp = vf.createURI("http://lab.linkeddata.deri.ie/test#advise");
			con.add(vf.createStatement(person1, adviseProp, person2));
			con.add(vf.createStatement(advisor, adviseProp, person3));
		
			//
			URI scienceCollege = vf.createURI("http://example.org/ScienceCollege");
			URI collegeProp = vf.createURI("http://lab.linkeddata.deri.ie/test#college");
			con.add(vf.createStatement(person2, collegeProp, scienceCollege));
			con.add(vf.createStatement(person3, collegeProp, scienceCollege));
			
			return model;
		}finally{
			con.close();
		}
	}
	
	private URI addFoafPerson(ValueFactory vf,RepositoryConnection con,String uri,String name,String email)throws Exception{
		URI person = vf.createURI(uri);
		con.add(vf.createStatement(person, RDF.TYPE, vf.createURI("http://xmlns.com/foaf/0.1/Person")));
		con.add(vf.createStatement(person,vf.createURI("http://xmlns.com/foaf/0.1/name"),vf.createLiteral(name)));
		con.add(vf.createStatement(person,vf.createURI("http://xmlns.com/foaf/0.1/mbox"),vf.createURI("mailto:" + email)));
		
		return person;
	}
}
