package org.deri.grefine.reconcile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.refine.util.ParsingUtilities;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.deri.grefine.reconcile.model.ReconciliationRequest;
import org.deri.grefine.reconcile.model.ReconciliationResponse;
import org.deri.grefine.reconcile.model.ReconciliationService;
import org.deri.grefine.reconcile.model.SearchResultItem;
import org.deri.grefine.reconcile.rdf.RdfReconciliationService;
import org.deri.grefine.reconcile.rdf.endpoints.PlainSparqlQueryEndpoint;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpoint;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpointImpl;
import org.deri.grefine.reconcile.rdf.executors.DumpQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.QueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.RemoteQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.VirtuosoRemoteQueryExecutor;
import org.deri.grefine.reconcile.rdf.factories.*;
import org.deri.grefine.reconcile.util.GRefineJsonUtilities;
import org.deri.grefine.reconcile.util.PrefixManager;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

public class ServiceRegistry {

    private Map<String, ReconciliationService> services;
    private GRefineJsonUtilities grefineJsonUtilities;
    private PrefixManager prefixManager;

    public ServiceRegistry(GRefineJsonUtilities jsonUtilities, PrefixManager prefixer) {
        services = new HashMap<String, ReconciliationService>();
        this.grefineJsonUtilities = jsonUtilities;
        this.prefixManager = prefixer;
    }

    public void addService(ReconciliationService service) {
        this.services.put(service.getId(), service);
    }

    public ReconciliationService removeService(String id) {
        return this.services.remove(id);
    }

    public Set<String> getServiceIds() {
        return new HashSet<String>(services.keySet());
    }

    public Set<ReconciliationService> getServices() { return new HashSet<ReconciliationService>(services.values()); }

    public ReconciliationService getService(String id, FileInputStream in) {
        ReconciliationService service = services.get(id);
        if (in != null) {
            service.initialize(in);
        }
        return service;
    }

    public void addAndSaveService(ReconciliationService service, FileOutputStream out) throws IOException {
        this.addService(service);
        service.save(out);
    }

    public void save(FileOutputStream out) throws JsonGenerationException, IOException {
        Writer writer = new OutputStreamWriter(out);
        try {
            JsonGenerator jsonWriter = ParsingUtilities.mapper.getFactory().createGenerator(writer);
            jsonWriter.writeStartObject();
            jsonWriter.writeFieldName("services");
            jsonWriter.writeStartArray();
            for (ReconciliationService service : this.services.values()) {
                service.writeAsJson(jsonWriter, true);
            }
            jsonWriter.writeEndArray();
            jsonWriter.writeEndObject();
            jsonWriter.flush();
            jsonWriter.close();
        } finally {
            writer.close();
        }
    }

    public boolean hasService(String id) {
        return services.containsKey(id);
    }

    public String metadata(ReconciliationService service, String baseUrl, String callback) {
        return grefineJsonUtilities.getServiceMetadataAsJsonP(service, callback, baseUrl);
    }

    public String multiReconcile(ReconciliationService service, String queries)
            throws IOException {
        ImmutableMap<String, ReconciliationRequest> multiQueryRequest = grefineJsonUtilities.getMultipleRequest(queries);
        ImmutableMap<String, ReconciliationResponse> multiResult = service.reconcile(multiQueryRequest);
        String response = grefineJsonUtilities.getMultipleResponse(multiResult, prefixManager).toString();
        return response;
    }

    public String suggestType(ReconciliationService service, String prefix, String callback)
            throws IOException {
        ImmutableList<SearchResultItem> results = service.suggestType(prefix);
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeSearchResult(results, prefix));
    }

    public String previewType(ReconciliationService service, String typeId, String callback)
            throws Exception {
        String html = service.getPreviewHtmlForType(typeId);
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeHtml(html, typeId));
    }

    public String previewProperty(ReconciliationService service, String propertyId, String callback)
            throws Exception {
        String html = service.getPreviewHtmlForProperty(propertyId);
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeHtml(html, propertyId));
    }

    public String previewEntity(ReconciliationService service, String entityId, String callback)
            throws Exception {
        String html = service.getPreviewHtmlForEntity(entityId);
        if (html == null) {
            return null;
        }
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeHtml(html, entityId));
    }

    public String suggestProperty(ReconciliationService service, String typeId, String prefix, String callback)
            throws IOException {
        ImmutableList<SearchResultItem> results;
        if (typeId == null || typeId.isEmpty()) {
            results = service.suggestProperty(prefix);
        } else {
            results = service.suggestProperty(prefix, typeId);
        }
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeSearchResult(results, prefix));
    }

    public String suggestEntity(ReconciliationService service, String prefix, String callback)
            throws IOException {
        ImmutableList<SearchResultItem> results = service.suggestEntity(prefix);
        return grefineJsonUtilities.getJsonP(callback, grefineJsonUtilities.jsonizeSearchResult(results, prefix));
    }

    public String previewResource(ReconciliationService service, String resourceId)
            throws Exception {
        return service.getPreviewHtmlForResource(resourceId);
    }

    public String getHtmlOfResourcePreviewTemplate(String previewUrl, String resourceId)
            throws Exception {
        String templatePath = "templates/resource_preview_template.vt";
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("resourceUri", resourceId);
        context.put("previewResourceUrl", previewUrl);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(templatePath);

        VelocityEngine templateEngine = new VelocityEngine();
        templateEngine.init();

        templateEngine.evaluate(context, writer, "rdf-reconcile-extension", new InputStreamReader(in));
        writer.close();
        String html = writer.toString();
        return html;
    }

    public void loadFromFile(FileInputStream in) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode tokener = objectMapper.readTree(in);
            JsonNode services = tokener.get("services");
            for (int i = 0; i < services.size(); i++) {
                JsonNode serviceObj = services.get(i);
                String type = serviceObj.get("type").asText();
                ReconciliationService service;
                if (type.equals("rdf")) {
                    service = loadRdfServiceFromJSON(serviceObj);
                } else {
                    //unknown service ignore
                    continue;
                }
                this.services.put(service.getId(), service);
            }
        } finally {
            in.close();
        }

    }

    private RdfReconciliationService loadRdfServiceFromJSON(JsonNode serviceObj)
            throws IOException {
        String serviceId = serviceObj.get("id").asText();
        List<String> searchPropertyUris = new ArrayList<String>();
        JsonNode propertiesArray = serviceObj.get("searchPropertyUris");
        for (int i = 0; i < propertiesArray.size(); i++) {
            searchPropertyUris.add(propertiesArray.get(i).asText());
        }
        QueryEndpoint endpoint = loadEndpointFromJSON(serviceObj.get("endpoint"));
        return new RdfReconciliationService(serviceId, serviceObj.get("name").asText(),
                ImmutableList.copyOf(searchPropertyUris), endpoint, serviceObj.get("matchThreshold").asDouble());
    }

    private QueryEndpoint loadEndpointFromJSON(JsonNode endpointObj) throws IOException {
        String type = endpointObj.get("type").asText();
        QueryExecutor executor = loadQueryExecutorFromJSON(endpointObj.get("queryExecutor"));
        SparqlQueryFactory factory = loadQueryFactoryFromJSON(endpointObj.get("queryFactory"));
        if (type.equals("plain")) {
            return new PlainSparqlQueryEndpoint((PlainSparqlQueryFactory) factory, executor);
        } else {
            //default
            return new QueryEndpointImpl(factory, executor);
        }

    }

    private QueryExecutor loadQueryExecutorFromJSON(JsonNode jsonObject)
            throws IOException {
        String type = jsonObject.get("type").asText();
        if (type.equals("dump")) {
            if (jsonObject.has("propertyUri")) {
                return new DumpQueryExecutor(jsonObject.get("propertyUri").asText());
            } else {
                return new DumpQueryExecutor();
            }
        } else {
            String url = jsonObject.get("sparql-url").asText();
            String graph = null;
            if (jsonObject.has("default-graph-uri")) {
                graph = jsonObject.get("default-graph-uri").asText();
            }
            if (type.equals("remote-virtuoso")) {
                return new VirtuosoRemoteQueryExecutor(url, graph);
            } else {
                //plain
                return new RemoteQueryExecutor(url, graph);
            }
        }
    }

    private SparqlQueryFactory loadQueryFactoryFromJSON(JsonNode factoryObj)
            throws IOException {
        String type = factoryObj.get("type").asText();
        if (type.equals("virtuoso")) {
            return new VirtuosoSparqlQueryFactory();
        } else if (type.equals("jena-text")) {
            return new JenaTextSparqlQueryFactory();
        } else if (type.equals("rdf4j-text")) {
            return new Rdf4jTextSparqlQueryFactory();
        } else if (type.equals("bigowlim")) {
            return new BigOwlImSparqlQueryFactory();
        } else {
            //plain
            return new PlainSparqlQueryFactory();
        }
    }

}
