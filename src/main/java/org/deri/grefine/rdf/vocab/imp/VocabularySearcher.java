package org.deri.grefine.rdf.vocab.imp;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import org.deri.grefine.rdf.vocab.IVocabularySearcher;
import org.deri.grefine.rdf.vocab.PrefixExistException;
import org.deri.grefine.rdf.vocab.RDFNode;
import org.deri.grefine.rdf.vocab.RDFSClass;
import org.deri.grefine.rdf.vocab.RDFSProperty;
import org.deri.grefine.rdf.vocab.SearchResultItem;
import org.deri.grefine.rdf.vocab.Vocabulary;
import org.deri.grefine.rdf.vocab.VocabularyImportException;
import org.deri.grefine.rdf.vocab.VocabularyImporter;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VocabularySearcher implements IVocabularySearcher {

	final static Logger logger = LoggerFactory.getLogger("vocabulary_searcher");

	private static final String CLASS_TYPE = "class";
	private static final String PROPERTY_TYPE = "property";
	// project id is always a number. it is safe to use this placeholder
	private static final String GLOBAL_VOCABULARY_PLACE_HOLDER = "g";

	private IndexWriter writer;
	private IndexSearcher searcher;
	private IndexReader r;

	private Directory _directory;
	
	public VocabularySearcher(File dir) throws IOException {
		_directory = FSDirectory.open(new File(dir, "luceneIndex").toPath());
		Analyzer a = new StandardAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(a);

		try {
			writer = new IndexWriter(_directory, conf);
			writer.commit();
		} catch (org.apache.lucene.index.IndexFormatTooOldException e) {
            writer.close();
			Files.move(new File(dir, "luceneIndex").toPath(),
					new File(dir, "luceneIndex_41").toPath(), StandardCopyOption.REPLACE_EXISTING);

			writer = new IndexWriter(_directory, conf);
			writer.commit();
		}
        r = DirectoryReader.open(FSDirectory.open(new File(dir, "luceneIndex").toPath()));
        searcher = new IndexSearcher(r);
	}

	@Override
	public void importAndIndexVocabulary(String name, String uri, String fetchUrl,VocabularyImporter importer)throws VocabularyImportException, VocabularyIndexException,PrefixExistException, CorruptIndexException, IOException {
		importAndIndexVocabulary(name, uri, fetchUrl, GLOBAL_VOCABULARY_PLACE_HOLDER, importer);
	}

	@Override
	public void importAndIndexVocabulary(String name, String uri, String fetchUrl, String projectId, VocabularyImporter importer) throws VocabularyImportException,VocabularyIndexException, PrefixExistException,
			CorruptIndexException, IOException {
		List<RDFSClass> classes = new ArrayList<RDFSClass>();
		List<RDFSProperty> properties = new ArrayList<RDFSProperty>();
		importer.importVocabulary(name, uri, fetchUrl, classes, properties);
		indexTerms(name, uri, projectId, classes, properties);
	}

	
	@Override
	public void importAndIndexVocabulary(String name, String uri, Repository repository, String projectId, VocabularyImporter importer) throws VocabularyImportException, VocabularyIndexException,
			PrefixExistException, CorruptIndexException, IOException {
		List<RDFSClass> classes = new ArrayList<RDFSClass>();
		List<RDFSProperty> properties = new ArrayList<RDFSProperty>();
		importer.importVocabulary(name, uri, repository, classes, properties);
		indexTerms(name, uri, projectId, classes, properties);
	}

	@Override
	public List<SearchResultItem> searchClasses(String str, String projectId)
			throws IOException {
		Query query = prepareQuery(str, CLASS_TYPE, projectId);
		TopDocs docs = searcher.search(query, getMaxDoc());
		return prepareSearchResults(docs);
	}

	@Override
	public List<SearchResultItem> searchProperties(String str, String projectId)
			throws IOException {
		Query query = prepareQuery(str, PROPERTY_TYPE, projectId);
		TopDocs docs = searcher.search(query, getMaxDoc());
		return prepareSearchResults(docs);
	}

	@Override
	public void deleteTermsOfVocabs(Set<Vocabulary> toRemove, String projectId)
			throws CorruptIndexException, IOException {
		for (Vocabulary v : toRemove) {
			deleteTerms(v.getName(), projectId);
		}
		this.update();
	}

	@Override
	public void addPredefinedVocabulariesToProject(long projectId)throws VocabularyIndexException, IOException{
		//get all documents of the global scope
		TopDocs docs = getDocumentsOfProjectId(GLOBAL_VOCABULARY_PLACE_HOLDER);
		//add all of them to project projectId
		addDocumentsToProject(docs,String.valueOf(projectId));
		
		this.update();
	}
	
	@Override
	public void update() throws CorruptIndexException, IOException {
		writer.commit();
		// TODO this shouldn't be required but it is not working without it...
		// check
		r.close();
		r = DirectoryReader.open(_directory);
		searcher = new IndexSearcher(r);
	}
	
	@Override
	public void synchronize(String projectId, Set<String> prefixes) throws IOException{
		Set<String> allPrefixes = getPrefixesOfProjectId(projectId);
		allPrefixes.removeAll(prefixes);
		if(!allPrefixes.isEmpty()){
			deletePrefixesOfProjectId(projectId,allPrefixes);
		}
		this.update();
	}
	
	@Override
	public void deleteTermsOfVocab(String vocabName, String projectId) throws CorruptIndexException, IOException {
		deleteTerms(vocabName, projectId);
		this.update();
	}

	/*
	 * Private methods
	 */
	private void deleteTerms(String prefix, String projectId)
			throws CorruptIndexException, IOException {
		if (projectId == null || projectId.isEmpty()) {
			throw new RuntimeException("projectId is null");
		}
		// "type":vocabulary AND "projectId":projectId AND "name":name
		// ("type": (class OR property) ) AND "projectId":projectId AND
		// "prefix":name
		BooleanQuery typeQuery = new BooleanQuery.Builder().
				add(new TermQuery(new Term("type", CLASS_TYPE)), Occur.SHOULD).
				add(new TermQuery(new Term("type", PROPERTY_TYPE)),
				Occur.SHOULD).build();

		BooleanQuery termsQuery = new BooleanQuery.Builder().add(typeQuery, Occur.MUST)
				.add(new TermQuery(new Term("projectId", projectId)),
				Occur.MUST)
				.add(new TermQuery(new Term("prefix", prefix)), Occur.MUST).build();

		writer.deleteDocuments(termsQuery);
	}

	private void indexTerms(String _name, String _uri, String projectId,
			List<RDFSClass> classes, List<RDFSProperty> properties)
			throws CorruptIndexException, IOException {
		for (RDFSClass c : classes) {
			indexRdfNode(c, CLASS_TYPE, projectId);
		}
		for (RDFSProperty p : properties) {
			indexRdfNode(p, PROPERTY_TYPE, projectId);
		}

		this.update();
	}

	private void indexRdfNode(RDFNode node, String type, String projectId)
			throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new StringField("type", type, Field.Store.YES));
		doc.add(new StringField("prefix", node.getVocabularyPrefix(), Field.Store.YES));
		String l = node.getLabel() == null ? "" : node.getLabel();
		doc.add(new TextField("label", l, Field.Store.YES));
		String d = node.getDescription() == null ? "" : node.getDescription();
		doc.add(new TextField("description", d, Field.Store.YES));
		doc.add(new StoredField("uri", node.getURI()));
		doc.add(new TextField("localPart", node.getLocalPart(), Field.Store.YES));
		doc.add(new StoredField("namespace", node.getVocabularyUri()));
		doc.add(new StringField("projectId", String.valueOf(projectId), Field.Store.NO));

        writer.addDocument(doc);
	}

	private Query prepareQuery(String s, String type, String projectId)
			throws IOException {
	    // q1.add(new TermQuery(new
		// Term("projectId",GLOBAL_VOCABULARY_PLACE_HOLDER)), Occur.SHOULD);
        BooleanQuery.Builder q1 = new BooleanQuery.Builder().add(new TermQuery(new Term("projectId", projectId)), Occur.MUST);

		BooleanQuery.Builder q2 = new BooleanQuery.Builder().add(new TermQuery(new Term("type", type)), Occur.MUST);

		BooleanQuery.Builder q = new BooleanQuery.Builder()
                .add(q1.build(), Occur.MUST)
                .add(q2.build(), Occur.MUST);

		if (s != null && s.trim().length() > 0) {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			if (s.indexOf(":") == -1) {
				// the query we need:
				// "projectId":projectId AND "type":type AND ("prefix":s* OR
				// "localPart":s* OR "label":s* OR "description":s*)
				BooleanQuery.Builder q3 = new BooleanQuery.Builder()
                        .add(new WildcardQuery(new Term("prefix", s + "*")),
						Occur.SHOULD);

				TokenStream stream = analyzer.tokenStream("localPart",
						new StringReader(s));
				// get the TermAttribute from the TokenStream
				CharTermAttribute termAtt = (CharTermAttribute) stream
						.addAttribute(CharTermAttribute.class);

				stream.reset();
								
				while (stream.incrementToken()) {
					String tmp = termAtt.toString() + "*";
					q3.add(new WildcardQuery(new Term("localPart", tmp)),
							Occur.SHOULD);
				}
				stream.close();
				stream.end();

				stream = analyzer.tokenStream("description",
						new StringReader(s));
				// get the TermAttribute from the TokenStream
				termAtt = (CharTermAttribute) stream
						.addAttribute(CharTermAttribute.class);

				stream.reset();
				while (stream.incrementToken()) {
					String tmp = termAtt.toString() + "*";
					q3.add(new WildcardQuery(new Term("description", tmp)),
							Occur.SHOULD);
				}
				stream.close();
				stream.end();

				stream = analyzer.tokenStream("label", new StringReader(s));
				// get the TermAttribute from the TokenStream
				termAtt = (CharTermAttribute) stream
						.addAttribute(CharTermAttribute.class);

				stream.reset();
				while (stream.incrementToken()) {
					String tmp = termAtt.toString() + "*";
					q3.add(new WildcardQuery(new Term("label", tmp)),
							Occur.SHOULD);
				}
				stream.close();
				stream.end();

				q.add(q3.build(), Occur.MUST);
				return q.build();
			} else {
				// the query we need:
				// "projectId":projectId AND "type":type AND ("prefix":p1 AND
				// "localPart":s*)
				String p1 = s.substring(0, s.indexOf(":"));
				String p2 = s.substring(s.indexOf(":") + 1);

				BooleanQuery.Builder q3 = new BooleanQuery.Builder();
				q3.add(new TermQuery(new Term("prefix", p1)), Occur.SHOULD);

				BooleanQuery.Builder q4 = new BooleanQuery.Builder();

				TokenStream stream = analyzer.tokenStream("localPart",
						new StringReader(p2));
				// get the TermAttribute from the TokenStream
				CharTermAttribute termAtt = (CharTermAttribute) stream
						.addAttribute(CharTermAttribute.class);

				stream.reset();
				if (!p2.isEmpty()) {
					while (stream.incrementToken()) {
						q4.add(new WildcardQuery(new Term("localPart", termAtt.toString()
								 + "*")), Occur.SHOULD);
					}
				}
				stream.close();
				stream.end();

				q.add(q3.build(), Occur.MUST);
				if (!p2.isEmpty()) {
					q.add(q4.build(), Occur.MUST);
				}
				return q.build();
			}
		} else {
			return q.build();
		}

	}

	private List<SearchResultItem> prepareSearchResults(TopDocs docs)
			throws CorruptIndexException, IOException {
		List<SearchResultItem> res = new ArrayList<SearchResultItem>();
		for (int i = 0; i < docs.totalHits; i++) {
			Document doc = searcher.doc(docs.scoreDocs[i].doc);
			String uri = doc.get("uri");
			String label = doc.get("label");
			String description = doc.get("description");
			String prefix = doc.get("prefix");
			String lPart = doc.get("localPart");

			SearchResultItem item = new SearchResultItem(uri, prefix, lPart,
					label, description);
			res.add(item);
		}

		return res;

	}
	
	private void addDocumentsToProject(TopDocs docs,String projectId) throws CorruptIndexException, IOException{
		for(int i=0;i<docs.totalHits;i++){
			Document doc = searcher.doc(docs.scoreDocs[i].doc);
			//TODO this needs to be changed into a more efficient impl
			Document newdoc = new Document();
			Iterator fieldsIter = doc.getFields().iterator();
			while(fieldsIter.hasNext()){
				newdoc.add((IndexableField)fieldsIter.next());
			}
			newdoc.removeField("projectId");
			newdoc.add(new StoredField("projectId",projectId));
			writer.addDocument(newdoc);
		}
	}
	
	private TopDocs getDocumentsOfProjectId(String projectId) throws IOException{
		//query for:
		// "projectId":projectId
		Query query = new TermQuery(new Term("projectId",projectId));
		return searcher.search(query, getMaxDoc());
	}
	
	private Set<String> getPrefixesOfProjectId(String projectId) throws IOException{
		//query for:
		// "projectId":projectId
		Set<String> prefixes = new HashSet<String>();
		Query query = new TermQuery(new Term("projectId",projectId));
		TopDocs docs =  searcher.search(query, getMaxDoc());
		for (int i = 0; i < docs.totalHits; i++) {
			Document doc = searcher.doc(docs.scoreDocs[i].doc);
			prefixes.add(doc.get("prefix"));
		}
		return prefixes;
	}
	private void deletePrefixesOfProjectId(String projectId, Set<String> toDelete) throws CorruptIndexException, IOException {
		if (projectId == null || projectId.isEmpty()) {
			throw new RuntimeException("projectId is null");
		}
		// "type":vocabulary AND "projectId":projectId AND ("prefix":prefix OR ...)
		BooleanQuery.Builder q = new BooleanQuery.Builder();
		Query query = new TermQuery(new Term("projectId",projectId));
		//TODO backward compatibility is broken here!!!!!!
//		Query typeQ = new TermQuery(new Term("type", "vocabulary"));
		
		BooleanQuery.Builder prefixQ = new BooleanQuery.Builder();
		for(String p:toDelete){
			Query pQ = new TermQuery(new Term("prefix",p));
			prefixQ.add(pQ,Occur.SHOULD);
		}
		q.add(query,Occur.MUST);
//		q.add(typeQ,Occur.MUST);
		q.add(prefixQ.build(),Occur.MUST);
		
		writer.deleteDocuments(q.build());
	}
	
	private int getMaxDoc() throws IOException {
		return r.maxDoc() > 0 ? r.maxDoc() : 100000;
	}
}
