package org.deri.grefine.reconcile.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;

import org.deri.grefine.rdf.utils.HttpUtils;
import org.deri.grefine.reconcile.model.ReconciliationStanbolSite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.commands.Command;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Command for adding Stanbol Reconciliation services
 *  
 * @author Sergio Fernández <sergio.fernandez@salzburgresearch.at>
 *
 */
public class AddStanbolServiceCommand extends Command {
	
	private static Logger log = LoggerFactory.getLogger(AddStanbolServiceCommand.class);
	private static String ENTITYHUB_PATH = "/entityhub/sites/referenced";
	private static String RECONCILE_SUFFIX = "reconcile";
	private static final String JSON = "application/json";
	private static final String RDFXML = "application/rdf+xml";
	
	/**
	 * Processes the pre-registration of an instance of Apache Stanbol,
	 * returning the list of suitable reconciliation services available
	 * 
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if(!hasValidCSRFToken(req)) {
			respondCSRFError(res);
			return;
		}
		String uri = req.getParameter("uri") + ENTITYHUB_PATH;
		log.debug("Requesting referenced site to Stanbol EntityHub '" + uri + "'...");
		Set<ReconciliationStanbolSite> reconciliations = retrieveReconciliations(uri);
		this.serializeReconciliations(res, reconciliations);
	}

	private Set<ReconciliationStanbolSite> retrieveReconciliations(String uri) throws IOException, JsonParseException, JsonMappingException {
		HttpEntity entity = HttpUtils.get(uri, JSON);
		ObjectMapper mapper = new ObjectMapper();
		Set<String> sites = mapper.readValue(entity.getContent(), new TypeReference<Set<String>>(){});
        Set<ReconciliationStanbolSite> reconciliations = new HashSet<ReconciliationStanbolSite>();
        for (String site : sites) {
        	ReconciliationStanbolSite reconciliation = this.buildReconciliation(site);
        	if (reconciliation != null) {
        		reconciliations.add(reconciliation);
        	} else {
        		log.error("'" + site + "' can't be correctly retrieved, so not going to be registered");
        	}
        }
        log.info("Retrieved " + reconciliations.size() + " suitable sites");
		return reconciliations;
	}

	private ReconciliationStanbolSite buildReconciliation(String site) throws IOException {
		HttpEntity entity = HttpUtils.get(site, RDFXML);
		Model model = ModelFactory.createDefaultModel();
		model.read(entity.getContent(), site);
		String query =  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
						"PREFIX entityhub: <http://stanbol.apache.org/ontology/entityhub/entityhub#> " +
						"SELECT ?name ?local " +
						"WHERE { " +
						"  <" + site + "> rdfs:label ?name ; " +
						"    entityhub:localMode ?local . " +
						"} ";
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		if (results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			ReconciliationStanbolSite reconciliation = new ReconciliationStanbolSite();
			reconciliation.setUri(site + RECONCILE_SUFFIX);
			reconciliation.setName(qs.getLiteral("name").getString());
			reconciliation.setLocal("true".equalsIgnoreCase(qs.getLiteral("local").getString()));
			return reconciliation;
		} else {
			return null;
		}
	}

	private void serializeReconciliations(HttpServletResponse res, Set<ReconciliationStanbolSite> reconciliations) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode json = mapper.createArrayNode();
		for (ReconciliationStanbolSite reconciliation : reconciliations) {
			json.add(reconciliation.getJSON());
		}
		res.setStatus(HttpServletResponse.SC_OK);
		res.setContentType(JSON);
		PrintWriter writer = res.getWriter();
		writer.println(json);
	}

}
