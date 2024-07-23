package org.deri.grefine.reconcile.commands;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.deri.grefine.reconcile.GRefineServiceManager;
import org.deri.grefine.reconcile.model.ReconciliationService;
import org.deri.grefine.reconcile.rdf.RdfReconciliationService;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpoint;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpointImpl;
import org.deri.grefine.reconcile.rdf.executors.DumpQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.QueryExecutor;
import org.deri.grefine.reconcile.rdf.factories.Rdf4jTextSparqlQueryFactory;
import org.deri.grefine.reconcile.rdf.factories.SparqlQueryFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.refine.util.ParsingUtilities;

import com.google.common.collect.ImmutableList;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;

public class UploadFileAndAddServiceCommand extends AbstractAddServiceCommand {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(!hasValidCSRFTokenAsHeader(request)) {
			respondCSRFError(response);
			return;
		}
		try{
			//write results
			response.setCharacterEncoding("UTF-8");
	        response.setHeader("Content-Type", "application/json");
	        PrintWriter out = response.getWriter();
			StringWriter sw = new StringWriter();
			JsonGenerator w = ParsingUtilities.mapper.getFactory().createGenerator(sw);
			w.writeStartObject();
	        w.writeStringField("code", "ok");
	        w.writeFieldName("service");
	        ReconciliationService service = getReconciliationService(request);
	        service.writeAsJson(w);
	        w.writeEndObject();
	        w.flush();
	        sw.flush();
			out.print(sw.toString());
	        out.flush();
		} catch (Exception e) {
			respondError(response,e);
		}
	}

	@Override
	protected ReconciliationService getReconciliationService(HttpServletRequest request) throws IOException {
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		InputStream in = null;
		String name=null, id = null, props = null, format=null, filename = null;
		Model model = null;
		try{
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item:items){
				if(item.getFieldName().equals("service_name")){
					name = item.getString(); 
					id = getIdForString(name);				
				}else if(item.getFieldName().equals("properties")){
					props = item.getString();
				}else if(item.getFieldName().equals("file_format")){
					format = item.getString();
				}else if(item.getFieldName().equals("file_upload")){
					filename = item.getName();
					in = item.getInputStream();
				}
			}
			
			if(format.equals("autodetect")){
				format = guessFormat(filename);
			}
            model = Rio.parse(in, "", rdfFormat(format));
			
			ImmutableList<String> propUris = asImmutableList(props);
		
			if(GRefineServiceManager.singleton.hasService(id)){
				//id already exist
				throw new RuntimeException("A service with name '" + id + "' already exist!");
			}
			
			if(name.isEmpty() || propUris.size()==0){
				throw new RuntimeException("name and at least one label property ar needed");
			}
			
			SparqlQueryFactory queryFactory = new Rdf4jTextSparqlQueryFactory();
			QueryExecutor queryExecutor;
			if(propUris.size()==1){
				queryExecutor = new DumpQueryExecutor(model,propUris.get(0));
			}else{
				queryExecutor = new DumpQueryExecutor(model);
			}
			QueryEndpoint queryEndpoint = new QueryEndpointImpl(queryFactory, queryExecutor);
			ReconciliationService service = new RdfReconciliationService(
					id, name, propUris, queryEndpoint, AddServiceCommand.DEFAULT_MATCH_THRESHOLD);
			GRefineServiceManager.singleton.addAndSaveService(service);

			return service;
		} catch(FileUploadException fe){
			throw new IOException(fe);
		}
	}

	private void respondError(HttpServletResponse response, Exception e) throws IOException, ServletException {
		try{
			String o = "{\"code\":\"error\"";
			o += ",\"message\": \"" + e.getMessage() + "\"";

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();

			o += ",\"stack\": \"" + sw.toString() + "\"}";

			response.setCharacterEncoding("UTF-8");
			respond(response, "<html><body><textarea>\n" + o + "\n</textarea></body></html>");
		} catch (IOException e1) {
            e1.printStackTrace(response.getWriter());
        }
	}

	private String guessFormat(String filename){
		if(filename.lastIndexOf('.')!=-1){
			String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
			if(extension.equals(".ttl")){
				return "TTL";
			}else if(extension.equals(".rdf")){
				return "RDF/XML";
			}else if(extension.equals(".owl")){
				return "RDF/XML";
			}else if(extension.equals(".nt")){
				return "N-TRIPLE";
			}else if(extension.equals(".n3")){
				return "N3";
			}
		}
		return "RDF/XML";
	}
}
