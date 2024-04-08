package org.deri.grefine.reconcile.rdf.executors;

import java.util.Collections;
import java.io.IOException;

import org.apache.jena.query.ResultSetFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

public class VirtuosoRemoteQueryExecutor extends RemoteQueryExecutor {

    public VirtuosoRemoteQueryExecutor(String sparqlEndpointUrl, String defaultGraphUri) {
        super(sparqlEndpointUrl, defaultGraphUri);
    }

    @Override
    public ResultSet sparql(String sparql) {
        // we use QueryEngineHTTP to skip query validation as Virtuoso
        // needs non-standardised extensions and will not pass ARQ validation
	QueryExecutionHTTPBuilder qBuilder = QueryExecutionHTTPBuilder.service(sparqlEndpointUrl);
	qBuilder.addDefaultGraphURI(defaultGraphUri);
	qBuilder.queryString(sparql);
        QueryExecutionHTTP qExec = qBuilder.build();
        //if (defaultGraphUri != null) {
        //    qExec.setDefaultGraphURIs(Collections.singletonList(defaultGraphUri));
        //}
        ResultSet results = null;
        try {
            ResultSet res = qExec.execSelect();
            results = ResultSetFactory.copyResults(res);
        } finally {
            qExec.close();
        }

        return results;
    }

    @Override
    public void write(JsonGenerator writer) throws JsonGenerationException, IOException {
        writer.writeStartObject();
        writer.writeStringField("type", "remote-virtuoso");
        writer.writeStringField("sparql-url", sparqlEndpointUrl);
        if (defaultGraphUri != null) {
            writer.writeStringField("default-graph-uri", defaultGraphUri);
        }
        writer.writeEndObject();
    }
}
