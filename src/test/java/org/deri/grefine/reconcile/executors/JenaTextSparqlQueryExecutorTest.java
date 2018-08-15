package org.deri.grefine.reconcile.executors;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import org.deri.grefine.reconcile.model.ReconciliationRequest;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.IdentifiedValueContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.PropertyContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.TextualValueContext;
import org.deri.grefine.reconcile.rdf.executors.DumpQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.QueryExecutor;
import org.deri.grefine.reconcile.rdf.factories.JenaTextSparqlQueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author fadmaa
 * this class mainly tests that queries produced by
 * {@link org.deri.grefine.reconcile.rdf.factories.JenaTextSparqlQueryFactory JenaTextSparqlQueryFactory}
 * are correct (executable)
 */
public class JenaTextSparqlQueryExecutorTest {

	private String classPrefix = "/org/deri/grefine/reconcile/resources/";

	QueryExecutor executor;
	JenaTextSparqlQueryFactory factory;
	
	//query
	int limit =8;
	String queryString = "godfather";
	ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label",
														"http://www.w3.org/2004/02/skos/core#prefLabel");
	
	@BeforeClass
	public void init(){
		Model m = ModelFactory.createDefaultModel();
		InputStream in = this.getClass().getResourceAsStream(classPrefix + "films.ttl");
		m.read(in,null,"TTL");
		
		executor = new DumpQueryExecutor(m);
		factory = new JenaTextSparqlQueryFactory();
	}
	
	@Test
	public void executeSimpleReconciliationQuery(){
		ReconciliationRequest request = new ReconciliationRequest(queryString, limit);
		String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);
		ResultSet resultset = executor.sparql(sparql);
		assertResult(null,resultset,"http://data.linkedmdb.org/resource/film/930","http://data.linkedmdb.org/resource/film/329",
		"http://data.linkedmdb.org/resource/film_series/261");
	}
	
	@Test
	public void executeReconciliationQueryWithType(){
		ReconciliationRequest request = new ReconciliationRequest(queryString, limit);
		request.setTypes(new String[] {"http://data.linkedmdb.org/resource/movie/film"});
		String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);
		ResultSet resultset = executor.sparql(sparql);
		assertResult("http://data.linkedmdb.org/resource/film_series/261", resultset,"http://data.linkedmdb.org/resource/film/930","http://data.linkedmdb.org/resource/film/329");
	}
	
	@Test
	public void executeReconciliationQueryWithContext(){
		ReconciliationRequest request = new ReconciliationRequest(queryString, limit);
		request.setTypes(new String[] {"http://data.linkedmdb.org/resource/movie/film"});
		
		PropertyContext prop1 = new PropertyContext("http://data.linkedmdb.org/resource/movie/initial_release_date", 
														new TextualValueContext("2006"));
		PropertyContext prop2 = new PropertyContext("http://data.linkedmdb.org/resource/movie/director", 
														new IdentifiedValueContext("http://data.linkedmdb.org/resource/movie/director3"));
		request.setContext(new ReconciliationRequestContext(prop1 ,prop2));
		String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);
		ResultSet resultset = executor.sparql(sparql);
		assertResult("http://data.linkedmdb.org/resource/film/329", resultset,"http://data.linkedmdb.org/resource/film/930");
	}
	
	private void assertResult(String notContainedUri, ResultSet resultset, String... containedUris){
		Set<String> urisSet = new HashSet<String>(Arrays.asList(containedUris));
		while(resultset.hasNext()){
			QuerySolution solution = resultset.nextSolution();
			Resource r = solution.getResource("entity");
			String uri = r.getURI();
			urisSet.remove(uri);
			if(uri.equals(notContainedUri)){
				fail(notContainedUri + " was found in the result") ;
			}
		}
		
		assertTrue(urisSet.isEmpty(), urisSet + " were not found in the result");
	}
	
	/*
	 * Suggest type 
	 */
	@Test
	public void suggestTypeTest(){
		String prefix = "fil";
		String sparql = factory.getTypeSuggestSparqlQuery(prefix, limit);
		ResultSet resultset = executor.sparql(sparql);
		assertInResultset("type"
                ,resultset
                ,"http://data.linkedmdb.org/resource/movie/film","http://data.linkedmdb.org/resource/movie/film_series");
	}
	
	private void assertInResultset(String propertyName, ResultSet resultset, String... containedUris){
		Set<String> urisSet = new HashSet<String>(Arrays.asList(containedUris));
		while(resultset.hasNext()){
			QuerySolution solution = resultset.nextSolution();
			Resource r = solution.getResource(propertyName);
			String uri = r.getURI();
			urisSet.remove(uri);
		}
		
		assertTrue(urisSet.isEmpty(), urisSet + " were not found in the result");
	}
	
	/*
	 * Suggest property 
	 */
	@Test
	public void suggestPropertyTest(){
		String prefix = "init";
		String sparql = factory.getPropertySuggestSparqlQuery(prefix, "http://data.linkedmdb.org/resource/movie/film", limit);
		ResultSet resultset = executor.sparql(sparql);
		assertInResultset("p", resultset,"http://data.linkedmdb.org/resource/movie/initial_release_date");
	}
	
	/*
	 * sample instances
	 */
	@Test
	public void sampleInstancesTest(){
		String sparql = factory.getSampleInstancesSparqlQuery(
		        "http://data.linkedmdb.org/resource/movie/film",
                ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label"),
                limit);
		ResultSet resultset = executor.sparql(sparql);
		assertTrue(resultset.hasNext());
		QuerySolution sol = resultset.nextSolution();
		assertFalse(sol.getLiteral("label1").getString().isEmpty());
		String uri1 = sol.getResource("entity").getURI();
		
		assertTrue(resultset.hasNext());
		sol = resultset.nextSolution();
		assertFalse(sol.getLiteral("label1").getString().isEmpty());
		assertFalse(uri1.equals(sol.getResource("entity").getURI()));
	}
	
	/*
	 * search entities
	 */
	@Test
	public void searchEntitiesTest(){
		String prefix = "godf";
		String sparql = factory.getEntitySearchSparqlQuery(prefix, searchPropertyUris, limit);
		ResultSet resultset = executor.sparql(sparql);
		assertInResultset("entity", resultset, "http://data.linkedmdb.org/resource/film_series/261",
					"http://data.linkedmdb.org/resource/film/930","http://data.linkedmdb.org/resource/film/329");
	}
}
