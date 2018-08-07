package org.deri.grefine.rdf.vocab;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.testng.annotations.Test;

import org.deri.grefine.rdf.vocab.imp.VocabularySearcher;

import static org.testng.Assert.*;

public class ImportPrefixTest {

	@Test
	public void testImportAndSeach()throws Exception{
		VocabularyImporter fakeImporter = new FakeImporter();
		VocabularySearcher searcher = new VocabularySearcher(new File("tmp"));
		searcher.importAndIndexVocabulary("foaf",
                "http://xmlns.com/foaf/0.1/",
                "http://xmlns.com/foaf/0.1/",
                "1",
                fakeImporter);
		
		assertFalse(searcher.searchClasses("foaf:P", "1").isEmpty());
	}
	
}

class FakeImporter extends VocabularyImporter{

	private String classPrefix = "/org/deri/grefine/vocab/resources/";

	@Override
	public void importVocabulary(String name, String uri, String fetchUrl,
                                 List<RDFSClass> classes, List<RDFSProperty> properties) throws VocabularyImportException {
		try{
			InputStream in = this.getClass().getResourceAsStream(classPrefix + "foaf.rdf");
			Repository repos = getRepository(in, RDFFormat.RDFXML);
			getTerms(repos, name, uri, classes, properties);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private Repository getRepository(InputStream in, RDFFormat format) throws Exception{
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		con.add(in, "", format);
		con.close();
		return repository;
	}
	
}
