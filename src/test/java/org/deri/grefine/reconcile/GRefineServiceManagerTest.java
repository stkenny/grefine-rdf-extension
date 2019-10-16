package org.deri.grefine.reconcile;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.deri.grefine.reconcile.model.ReconciliationRequest;
import org.deri.grefine.reconcile.model.ReconciliationService;
import org.deri.grefine.reconcile.rdf.RdfReconciliationService;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpointImpl;
import org.deri.grefine.reconcile.rdf.executors.DumpQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.RemoteQueryExecutor;
import org.deri.grefine.reconcile.rdf.factories.JenaTextSparqlQueryFactory;
import org.deri.grefine.reconcile.util.GRefineJsonUtilitiesImpl;

import org.shaded.apache.jena.rdf.model.Model;
import org.shaded.apache.jena.rdf.model.ModelFactory;
import org.apache.commons.io.FileUtils;

import com.google.refine.util.ParsingUtilities;

public class GRefineServiceManagerTest {

	String url = "http://example.org/endpoint";
	File dir = new File("tmp");
	
	@BeforeClass
	public void setUp() throws IOException{
		//empty dir if it exists
		if(dir.exists()){
			FileUtils.deleteDirectory(dir);
		}

		boolean res = dir.mkdir();
		if(!res){
			throw new IOException("unable to create " + dir);
		}
	
		File file = new File(dir,"services");
		file.createNewFile();
		
	}
	
	@Test
	public void saveServiceTest() throws IOException{
		String id = "sparql-test";
		ServiceRegistry registry = new ServiceRegistry(new GRefineJsonUtilitiesImpl(),null);
		GRefineServiceManager manager = new GRefineServiceManager(registry, dir);
		
		ReconciliationService service = new RdfReconciliationService(
		        id,
                id,
                new QueryEndpointImpl(
                        new JenaTextSparqlQueryFactory(),
                        new RemoteQueryExecutor(url, null)),
                0);
		manager.addService(service);
		
		assertTrue(registry.hasService(id));
		//verify service is saved
		
		registry = new ServiceRegistry(new GRefineJsonUtilitiesImpl(),null);
		//verify no service
		assertFalse(registry.hasService(id));
		
		File file = new File(dir,"services");
		//verify saved
		assertTrue(file.exists());
		
		FileInputStream in = new FileInputStream(file);

		registry.loadFromFile(in);
		//verify service is loaded
		verifyCorrectService(registry.getService(id, null), service);
	}
	
	@Test
	public void saveRdfServiceTest() throws IOException{
		String id = "rdf-test";
		ServiceRegistry registry = new ServiceRegistry(new GRefineJsonUtilitiesImpl(), null);
		GRefineServiceManager manager = new GRefineServiceManager(registry, dir);
		
		Model m = ModelFactory.createDefaultModel();
		ReconciliationService service = new RdfReconciliationService(id, id,
				new QueryEndpointImpl(new JenaTextSparqlQueryFactory(),
				new DumpQueryExecutor(m)), 0);
		manager.addAndSaveService(service);
		
		assertTrue(registry.hasService(id));
		//verify service is saved
		
		registry = new ServiceRegistry(new GRefineJsonUtilitiesImpl(),null);
		//verify no service
		assertFalse(registry.hasService(id));
		
		File file = new File(dir,"services");
		//verify saved
		assertTrue(file.exists());
		
		FileInputStream in = new FileInputStream(file);

		registry.loadFromFile(in);
		//verify service is loaded
		ReconciliationService service2 = registry.getService(id, null);
		verifyCorrectService(service2, service);
		//verify service is not initialized
		ReconciliationRequest request = new ReconciliationRequest("query", 10);
		String msg = "";
		try{
			service2.reconcile(request);
		}catch(RuntimeException e){
			msg = e.getMessage();
		}
		assertTrue(msg.equals("Model is not loaded"));
		
		FileInputStream modelIn = new FileInputStream(new File(dir,id + ".ttl"));
		ReconciliationService service3 = registry.getService(id, modelIn);
		assertTrue(service3.reconcile(request).getResults().isEmpty());
	}

	private void verifyCorrectService(ReconciliationService service,ReconciliationService expected) throws IOException, JsonGenerationException {
		StringWriter w1 = new StringWriter();
		JsonGenerator j1 = ParsingUtilities.mapper.getFactory().createGenerator(w1);
		StringWriter w2 = new StringWriter();
		JsonGenerator j2 = ParsingUtilities.mapper.getFactory().createGenerator(w2);
		service.writeAsJson(j1);
		expected.writeAsJson(j2);
		w1.flush(); w2.flush();
		assertEquals(w1.toString(), w2.toString());
	}
}
