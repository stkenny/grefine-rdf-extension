package org.deri.grefine.reconcile.rdf.executors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author fadmaa
 * execute SPARQL queries against Dump RDF and supports LARQ for
 * full text search
 * as index in built with the model this class can be costly to build
 * consider sharing instances of this class It is thread-safe
 */
public class DumpQueryExecutor implements QueryExecutor {

    private Repository index;
    private boolean loaded = false;
    //property used for index/search (only if one property is used)
    private String propertyUri;

    public DumpQueryExecutor() {
    }

    public DumpQueryExecutor(String propertyUri) {
        this.propertyUri = propertyUri;
    }

    public DumpQueryExecutor(Model m, String propUri) {
        this(m, propUri, false, DEFAULT_MIN_NGRAM, DEFAULT_MAX_NGRAM);
    }

    public DumpQueryExecutor(Model m) {
        this(m, null, false, DEFAULT_MIN_NGRAM, DEFAULT_MAX_NGRAM);
    }

    public DumpQueryExecutor(Model m, String propertyUri, boolean ngramIndex, int minGram, int maxGram) {
        this.propertyUri = propertyUri;

        Sail baseSail = new MemoryStore();
        LuceneSail luceneSail = new LuceneSail();
        luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
        if(propertyUri != null){
           luceneSail.setParameter(LuceneSail.INDEXEDFIELDS, "index.1=" + propertyUri); 
        }
        // wrap base sail
        luceneSail.setBaseSail(baseSail);
        this.index = new SailRepository(luceneSail);

        // Open a connection to the database
        try (RepositoryConnection conn = this.index.getConnection()) {
            conn.add(m);
        }
        this.loaded = true;
    }

    @Override
    public ResultSet sparql(String sparql){
        if (!loaded) {
            throw new RuntimeException("Model is not loaded");
        }
        ResultSet results = null;
        try (RepositoryConnection con = this.index.getConnection()) {
            TupleQuery tupleQuery = con.prepareTupleQuery(sparql);
            TupleQueryResult result = tupleQuery.evaluate();
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            QueryResultIO.writeTuple(result, TupleQueryResultFormat.SPARQL, boas);
            results = ResultSetFactory.fromXML(new ByteArrayInputStream(boas.toByteArray()));
        } catch(IOException e){
            throw new RuntimeException("Error querying model");
        }

        return results;
    }

    @Override
    public void write(JsonGenerator writer) throws JsonGenerationException, IOException {
        writer.writeStartObject();
        writer.writeStringField("type", "dump");
        if (propertyUri != null) {
            writer.writeStringField("propertyUri", propertyUri);
        }
        writer.writeEndObject();
    }

    public void dispose() {
        this.index.shutDown();
        this.index = null; //free the memory used for the model
    }

    public synchronized void initialize(InputStream in) {
        if (this.loaded) {
            return;
        }
        try {
            Model model = Rio.parse(in, "", RDFFormat.TURTLE);

            Sail baseSail = new MemoryStore();
            LuceneSail luceneSail = new LuceneSail();
            luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
            if(propertyUri != null){
                luceneSail.setParameter(LuceneSail.INDEXEDFIELDS, "index.1=" + propertyUri); 
            }

            // wrap base sail
            luceneSail.setBaseSail(baseSail);
            this.index = new SailRepository(luceneSail);

            // Open a connection to the database
            try (RepositoryConnection conn = this.index.getConnection()) {
              // add the model
              conn.add(model);
            }
            this.loaded = true;
        } catch(IOException e){
            this.loaded = false;
        } 
    }
   
    private static final int DEFAULT_MIN_NGRAM = 3;
    private static final int DEFAULT_MAX_NGRAM = 3;

    @Override
    public void save(String serviceId, FileOutputStream out) throws IOException {
        try (RepositoryConnection conn = this.index.getConnection()) {
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            // use pretty-printing (nice indentation)
            writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
            // inline blank nodes where possible
            writer.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            conn.export(writer);
        } finally {
            out.close();
        }
    }
}