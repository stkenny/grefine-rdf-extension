package org.deri.grefine.rdf.vocab;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.commands.AddPrefixCommand;
import org.deri.grefine.rdf.vocab.imp.VocabularySearcher;

import static org.testng.Assert.*;

public class AddPrefixCommandTest{
	private static final String TEMP_TEST_DIRECTORY = "tmp_VocabularySearchRelatedCommandsTest";
	ApplicationContext ctxt;
	VocabularySearcher searcher;
	VocabularyImporter importer;
	
	String name= "foaf";
	String uri = "http://xmlns.com/foaf/0.1/";
	String projectId = "1";

	Path tempDir;
	
	@BeforeClass
	public void init() throws IOException{
		tempDir = Files.createTempDirectory("tmp_VocabularySearchRelatedCommandsTest");
		importer = new FakeImporter();
		searcher = new FakeVocabularySearcher(tempDir.toFile(),this.importer);
		ctxt = new ApplicationContext();
		ctxt.setVocabularySearcher(searcher);
	}

	@AfterClass
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir.toFile());
    }

	@Test
	public void testAddPrefixCommand() throws Exception{
		RdfSchema schema = new RdfSchema();
		AddPrefixCommand command = new FakeAddPrefixCommand(ctxt,schema);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.addParameter("name", name);
		request.addParameter("uri", uri);
		request.addParameter("fetch", "web");
		request.addParameter("project", projectId);
		request.addParameter("fetch-url", uri);
		
		assertFalse(schema.getPrefixesMap().containsKey("foaf"));
		assertTrue(searcher.searchClasses("foaf:P", projectId).isEmpty());
		command.doPost(request, response);
		//verification
		
		//prefix is added to the project
		assertTrue(schema.getPrefixesMap().containsKey("foaf"));
		//search
		assertFalse(searcher.searchClasses("foaf:P", projectId).isEmpty());
	}
}

class FakeVocabularySearcher extends VocabularySearcher{

	VocabularyImporter importer;
	
	public FakeVocabularySearcher(File dir,VocabularyImporter importer) throws IOException {
		super(dir);
		this.importer = importer;
	}

	@Override
	public void importAndIndexVocabulary(String name, String uri,
			String fetchUrl, String projectId, VocabularyImporter im)
			throws VocabularyImportException, VocabularyIndexException, PrefixExistException, CorruptIndexException, IOException {
		super.importAndIndexVocabulary(name, uri, fetchUrl, projectId, this.importer);
	}
}

class FakeAddPrefixCommand extends AddPrefixCommand{

	RdfSchema schema;
	public FakeAddPrefixCommand(ApplicationContext ctxt,RdfSchema schema) {
		super(ctxt);
		this.schema = schema;
	}

	@Override
	public RdfSchema getRdfSchema(HttpServletRequest request)
			throws ServletException {
		return schema;
	}
	
	
}
