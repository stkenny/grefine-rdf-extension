package org.deri.grefine.reconcile.factories;

import static org.testng.Assert.assertEquals;

import org.deri.grefine.reconcile.rdf.factories.JenaTextSparqlQueryFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import org.deri.grefine.reconcile.model.ReconciliationRequest;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.IdentifiedValueContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.PropertyContext;
import org.deri.grefine.reconcile.model.ReconciliationRequestContext.TextualValueContext;
import org.deri.grefine.reconcile.rdf.factories.SparqlQueryFactory;


public class JenaTextSparqlQueryFactoryTest {

    int limit = 10;
    String query = "Fadi Maali";
    SparqlQueryFactory factory ;

    @BeforeMethod
    public void setUp(){
        factory = new JenaTextSparqlQueryFactory();
    }


    /*
     * RECONCILIATION QUERIES TESTS
     */

    @Test
    public void multiLabelsSimpleReconciliationTest(){
        ReconciliationRequest request = new ReconciliationRequest(query, limit);
        //this will assure that empty string for type is ignored
        request.setTypes(new String[] {""});
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2004/02/skos/core#prefLabel");
        String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);

        String expected =
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX text:<http://jena.apache.org/text#> " +
                        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                        "SELECT ?entity ?label " +
                        "WHERE" +
                        "{" +
                        "?entity ?p (?label 'Fadi Maali' 20).?entity ?p ?label." +
                        "FILTER (?p=<http://www.w3.org/2000/01/rdf-schema#label> || ?p=<http://www.w3.org/2004/02/skos/core#prefLabel>)" +
                        " FILTER (isIRI(?entity))}GROUP BY ?entity ?label" +
                        " LIMIT "  + String.valueOf(limit * searchPropertyUris.size());

        assertEquals(sparql, expected);
    }

    @Test
    public void multiLabelsWithTypeSimpleReconciliationTest(){
        ReconciliationRequest request = new ReconciliationRequest(query, limit);
        request.setTypes(new String[] {"http://xmlns.com/foaf/0.1/Person" , "http://example.org/ontology/Person"});
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2004/02/skos/core#prefLabel");
        String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);

        String expected =
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX text:<http://jena.apache.org/text#> " +
                        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                        "SELECT ?entity ?label " +
                        "WHERE" +
                        "{" +
                        "?entity ?p (?label 'Fadi Maali' 20).?entity ?p ?label." +
                        "FILTER (?p=<http://www.w3.org/2000/01/rdf-schema#label> || ?p=<http://www.w3.org/2004/02/skos/core#prefLabel>) " +
                        "{" +
                        "{?entity rdf:type <http://xmlns.com/foaf/0.1/Person>. } " +
                        "UNION " +
                        "{?entity rdf:type <http://example.org/ontology/Person>. }" +
                        "} FILTER (isIRI(?entity))" +
                        "}GROUP BY ?entity ?label " +
                        "LIMIT "  + String.valueOf(limit * searchPropertyUris.size());

        assertEquals(sparql, expected);
    }

    @Test
    public void multiLabelsWithTypeWithContextReconciliationTest(){
        ReconciliationRequest request = new ReconciliationRequest(query, limit);
        request.setTypes(new String[] {"http://xmlns.com/foaf/0.1/Person" , "http://example.org/ontology/Person"});

        PropertyContext prop1 = new PropertyContext("http://example.org/ontology/worksFor", new IdentifiedValueContext("http://example.org/resource/DERI"));
        PropertyContext prop2 = new PropertyContext("http://xmlns.com/foaf/0.1/nick", new TextualValueContext("fadmaa"));
        request.setContext(new ReconciliationRequestContext(prop1, prop2));
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2004/02/skos/core#prefLabel");
        String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);

        String expected =
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX text:<http://jena.apache.org/text#> " +
                        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                        "SELECT ?entity ?label " +
                        "WHERE" +
                        "{" +
                        "?entity ?p (?label 'Fadi Maali' 20).?entity ?p ?label." +
                        "FILTER (?p=<http://www.w3.org/2000/01/rdf-schema#label> || ?p=<http://www.w3.org/2004/02/skos/core#prefLabel>) " +
                        "{" +
                        "{?entity rdf:type <http://xmlns.com/foaf/0.1/Person>. } " +
                        "UNION " +
                        "{?entity rdf:type <http://example.org/ontology/Person>. }" +
                        "}" +
                        "?entity <http://example.org/ontology/worksFor> <http://example.org/resource/DERI>. " +
                        "?entity <http://xmlns.com/foaf/0.1/nick> 'fadmaa'.  FILTER (isIRI(?entity))" +
                        "}GROUP BY ?entity ?label " +
                        "LIMIT "  + String.valueOf(limit * searchPropertyUris.size());

        assertEquals(sparql, expected);
    }

    /*
     * ONE-LABEL-PROPERTY (OPTIMIZED) RECONCILIATION QUERIES TESTS
     */
    @Test
    public void oneLabelsSimpleReconciliationTest(){
        ReconciliationRequest request = new ReconciliationRequest(query, limit);
        //this will assure that empty string for type is ignored
        request.setTypes(new String[] {""});
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label");
        String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);

        String expected =
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX text:<http://jena.apache.org/text#> " +
                        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                        "SELECT ?entity ?label " +
                        "WHERE " +
                        "{ " +
                        "?entity text:query (<http://www.w3.org/2000/01/rdf-schema#label> 'Fadi Maali' 10) . " +
                        "?entity <http://www.w3.org/2000/01/rdf-schema#label> ?label ." +
                        "}GROUP BY ?entity ?label " +
                        "ORDER BY DESC(?score1) LIMIT "  + String.valueOf(limit * searchPropertyUris.size());

        assertEquals(sparql, expected);
    }

    @Test
    public void oneLabelsWithTypeSimpleReconciliationTest(){
        ReconciliationRequest request = new ReconciliationRequest(query, limit);
        request.setTypes(new String[] {"http://xmlns.com/foaf/0.1/Person" , "http://example.org/ontology/Person"});
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2004/02/skos/core#prefLabel");
        String sparql = factory.getReconciliationSparqlQuery(request, searchPropertyUris);

        String expected =
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX text:<http://jena.apache.org/text#> " +
                        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                        "SELECT ?entity ?label "  +
                        "WHERE { ?entity text:query (<http://www.w3.org/2004/02/skos/core#prefLabel> 'Fadi Maali' 10) . " +
                        "?entity <http://www.w3.org/2004/02/skos/core#prefLabel> ?label . " +
                        "{{?entity rdf:type <http://xmlns.com/foaf/0.1/Person>. } " +
                        "UNION " +
                        "{?entity rdf:type <http://example.org/ontology/Person>. }}}" +
                        "GROUP BY ?entity ?label ORDER BY DESC(?score1) LIMIT " + String.valueOf(limit * searchPropertyUris.size());

        assertEquals(sparql, expected);
    }

    /*
     * GUESS TYPE TESTS
     */
    @Test
    public void typesOfEntitiesTest(){
        String sparql = factory.getTypesOfEntitiesQuery(ImmutableList.of("http://example.org/resource/DERI","http://anothernamespace.org/resource#me"));
        String expected =
                "SELECT ?entity ?type " +
                        "WHERE{ " +
                        "?entity a ?type. " +
                        "FILTER (?entity=<http://example.org/resource/DERI> || ?entity=<http://anothernamespace.org/resource#me>). " +
                        "}";
        assertEquals(sparql, expected);
    }

    /*
     * SUGGEST TYPE TESTS
     */

    @Test
    public void suggestTypeTest(){
        String prefix = "Pers";
        String sparql = factory.getTypeSuggestSparqlQuery(prefix, limit);

        String expected =
                "PREFIX text:<http://jena.apache.org/text#> " +
                "SELECT DISTINCT ?type ?label1 ?label2  " +
                "WHERE{[] a ?type. " +
                "{OPTIONAL {?type <http://www.w3.org/2000/01/rdf-schema#label> (?label1  '" + prefix + "*' 10 ) . " +
                "?type <http://www.w3.org/2000/01/rdf-schema#label>  ?label1 . }" +
                "OPTIONAL {?type <http://www.w3.org/2004/02/skos/core#prefLabel> (?label2 '" + prefix + "*' 10 )." +
                "?type <http://www.w3.org/2004/02/skos/core#prefLabel> ?label2.} " +
                "FILTER (bound(?label1) || bound(?label2))}} LIMIT " + limit;

        assertEquals(sparql, expected);
    }

    /*
     * SUGGEST PROPERTY TESTS
     */

    @Test
    public void suggestPropertyWithSpecificSubjectsTypeTest(){
        String prefix = "labe";
        String typeUri = "http://xmlns.com/foaf/0.1/Person";
        String sparql = factory.getPropertySuggestSparqlQuery(prefix, typeUri, limit);

        String expected =
                "PREFIX text:<http://jena.apache.org/text#> " +
                "SELECT DISTINCT ?p ?label1  ?label2 " +
                "WHERE{[] a <http://xmlns.com/foaf/0.1/Person>; ?p ?v. " +
                "{OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> (?label1 '" + prefix + "*' 10). " +
                "?p <http://www.w3.org/2000/01/rdf-schema#label> ?label1. }" +
                "OPTIONAL {?p <http://www.w3.org/2004/02/skos/core#prefLabel> (?label2 '" + prefix + "*' 10). " +
                        "?p <http://www.w3.org/2004/02/skos/core#prefLabel> ?label2. }" +
                        "FILTER (bound(?label1) || bound(?label2))}} LIMIT " + limit;

        assertEquals(sparql, expected);
    }

    @Test
    public void suggestPropertyTest(){
        String prefix = "labe";
        String sparql = factory.getPropertySuggestSparqlQuery(prefix, limit);

        String expected =
                "PREFIX text:<http://jena.apache.org/text#> "+
                "SELECT DISTINCT ?p ?label1 ?label2 " +
                "WHERE{[] ?p ?v. " +
                "{OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> (?label1 '" + prefix + "*'  10). " +
                "?p <http://www.w3.org/2000/01/rdf-schema#label> ?label1. }" +
                "OPTIONAL {?p <http://www.w3.org/2004/02/skos/core#prefLabel> (?label2 '" + prefix + "*' 10). " +
                "?p <http://www.w3.org/2004/02/skos/core#prefLabel> ?label2. }" +
                "FILTER (bound(?label1) || bound(?label2))}} LIMIT " + limit;

        assertEquals(sparql, expected);
    }

    /*
     * SAMPLE INSTANCES
     */
    @Test
    public void sampleInstances(){
        String sparql = factory.getSampleInstancesSparqlQuery("http://data.linkedmdb.org/resource/movie/film", ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label"), 10);
        String expected =
                "SELECT ?entity (SAMPLE(?label) AS ?label1) " +
                        "WHERE{" +
                        "?entity a <http://data.linkedmdb.org/resource/movie/film>. " +
                        "?entity <http://www.w3.org/2000/01/rdf-schema#label> ?label." +
                        "}GROUP BY ?entity LIMIT 10"
                ;
        assertEquals(sparql, expected);
    }

    /*
     * ENTITY SEARCH
     */

    @Test
    public void entitySearchTest(){
        ImmutableList<String> searchPropertyUris = ImmutableList.of("http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2004/02/skos/core#prefLabel");
        String prefix = "fad";
        String sparql = factory.getEntitySearchSparqlQuery(prefix,searchPropertyUris, 10);
        String expected =
                "PREFIX text:<http://jena.apache.org/text#> " +
                        "SELECT ?entity ?label " +
                        "WHERE{" +
                        "?entity ?label_prop (?label '"+ prefix + "*' 20) . ?entity ?label_prop ?label . " +
                        "FILTER (?label_prop=<http://www.w3.org/2000/01/rdf-schema#label> || ?label_prop=<http://www.w3.org/2004/02/skos/core#prefLabel>). " +
                        "} LIMIT " + limit*2;
        ;
        assertEquals(sparql, expected);
    }
}

