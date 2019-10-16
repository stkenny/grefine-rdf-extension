package org.deri.grefine.reconcile.rdf.executors;

import org.shaded.apache.jena.query.*;
import org.shaded.apache.jena.query.text.EntityDefinition;
import org.shaded.apache.jena.query.text.TextDatasetFactory;
import org.shaded.apache.jena.query.text.TextIndexConfig;
import org.shaded.apache.jena.rdf.model.Model;
import org.shaded.apache.jena.rdf.model.ModelFactory;
import org.shaded.apache.jena.vocabulary.RDFS;
import org.shaded.apache.jena.vocabulary.SKOS;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author fadmaa
 * execute SPARQL queries against Dump RDF and supports LARQ for
 * full text search
 * as index in built with the model this class can be costly to build
 * consider sharing instances of this class It is thread-safe
 */
public class DumpQueryExecutor implements QueryExecutor {

    private Dataset index;
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
        loaded = true;
        this.propertyUri = propertyUri;

        Dataset dataset = DatasetFactory.create();
        EntityDefinition entDef = createEntityDefinition(m);
        // Lucene, in memory.
        Directory dir = new RAMDirectory();

        // Join together into a dataset
        this.index = TextDatasetFactory.createLucene(dataset, dir, new TextIndexConfig(entDef));
        this.index.getDefaultModel().add(m);
    }

    @Override
    public ResultSet sparql(String sparql) {
        if (!loaded) {
            throw new RuntimeException("Model is not loaded");
        }
        this.index.begin(ReadWrite.READ);

        Query query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
        QueryExecution qExec = QueryExecutionFactory.create(query, this.index);
        ResultSet result = qExec.execSelect();

        this.index.end();

        return result;
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
        this.index.close();
        this.index = null; //free the memory used for the model
    }

    public synchronized void initialize(FileInputStream in) {
        if (this.loaded) {
            return;
        }
        this.loaded = true;
        // -- Read and index all literal strings.
        Model model = ModelFactory.createDefaultModel();
        model.read(in, null, "TTL");

        Dataset dataset = DatasetFactory.create();
        EntityDefinition entDef = createEntityDefinition(model);

        // Lucene, in memory.
        Directory dir = new RAMDirectory();

        // Join together into a dataset
        Dataset luceneDataset = TextDatasetFactory.createLucene(dataset, dir, new TextIndexConfig(entDef));
        luceneDataset.begin(ReadWrite.WRITE);
        try {
            luceneDataset.getDefaultModel().add(model);
            luceneDataset.commit();
        } finally {
            luceneDataset.end();
        }
        this.index = luceneDataset;
    }

    private EntityDefinition createEntityDefinition(Model model){
        EntityDefinition entDef = new EntityDefinition(
                "uri",
                "text",
                 model.getResource(propertyUri));
        entDef.set("label", RDFS.label.asNode());
        entDef.set("prefLabel", SKOS.prefLabel.asNode());

        return entDef;
    }

    private static final int DEFAULT_MIN_NGRAM = 3;
    private static final int DEFAULT_MAX_NGRAM = 3;

    @Override
    public void save(String serviceId, FileOutputStream out) throws IOException {
        this.index.getDefaultModel().write(out, "TTL");
        out.close();
    }
}