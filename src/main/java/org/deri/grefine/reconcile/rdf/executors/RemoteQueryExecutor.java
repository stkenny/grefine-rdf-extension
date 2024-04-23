package org.deri.grefine.reconcile.rdf.executors;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;

/**
 * @author fadmaa
 * query a remote SPARQL endpoint
 */
public class RemoteQueryExecutor implements QueryExecutor{
	protected String sparqlEndpointUrl;
	protected String defaultGraphUri;
	
	public RemoteQueryExecutor(String sparqlEndpointUrl, String defaultGraphUri) {
		this.sparqlEndpointUrl = sparqlEndpointUrl;
		this.defaultGraphUri = defaultGraphUri;
	}

	@Override
	public ResultSet sparql(String sparql) {
		QueryExecution qExec;
		if(defaultGraphUri==null){
			qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, sparql);
		}else{
			qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, sparql, defaultGraphUri);
		}
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
	public void save(String serviceId, FileOutputStream baseDir) throws IOException {
		//nothing to save... all data is external
	}
	
	@Override
	public void write(JsonGenerator writer) throws JsonGenerationException, IOException {
		writer.writeStartObject();
		writer.writeStringField("type", "remote");
		writer.writeStringField("sparql-url", sparqlEndpointUrl);
		if(defaultGraphUri!=null){
			writer.writeStringField("default-graph-uri", defaultGraphUri);
		}
		writer.writeEndObject();
	}

	@Override
	public void initialize(InputStream in) {
		//nothing to initialize
	}
}
