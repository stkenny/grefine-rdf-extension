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

import org.deri.grefine.rdf.vocab.RDFSClass;
import org.deri.grefine.rdf.vocab.RDFSProperty;
import org.deri.grefine.rdf.vocab.VocabularyImportException;
import org.deri.grefine.rdf.vocab.VocabularyImporter;
import org.deri.grefine.rdf.vocab.imp.VocabularySearcher;

import static org.testng.Assert.*;

public class ImportPrefixTest {

	@Test
	public void testImportAndSeach()throws Exception{
		VocabularyImporter fakeImporter = new FakeImporter();
		VocabularySearcher searcher = new VocabularySearcher(new File("tmp"));
		searcher.importAndIndexVocabulary("foaf", "http://xmlns.com/foaf/0.1/", "http://xmlns.com/foaf/0.1/","1", fakeImporter);
		
		assertFalse(searcher.searchClasses("foaf:P", "1").isEmpty());
	}
	
}

class FakeImporter extends VocabularyImporter{

	@Override
	public void importVocabulary(String name, String uri, String fetchUrl,
			List<RDFSClass> classes, List<RDFSProperty> properties)	throws VocabularyImportException {
		try{
			InputStream in = getClass().getResourceAsStream("../../org/deri/reconcile/files/foaf.rdf");
			Repository repos = getRepository(in,RDFFormat.RDFXML);
			getTerms(repos, name, uri, classes, properties);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private Repository getRepository(InputStream in, RDFFormat format) throws Exception{
		Repository therepository = new SailRepository(new MemoryStore());
		therepository.initialize();
		RepositoryConnection con = therepository.getConnection();
		con.add(in, "", format);
		con.close();
		return therepository;
	}
	
}
