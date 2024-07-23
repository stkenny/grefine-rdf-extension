package org.deri.grefine.reconcile.commands;

import com.google.common.collect.ImmutableList;
import com.google.refine.commands.Command;
import org.apache.commons.lang.StringUtils;
import org.deri.grefine.reconcile.model.ReconciliationService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.refine.util.ParsingUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.rdf4j.rio.RDFFormat;

public abstract class AbstractAddServiceCommand extends Command{

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!hasValidCSRFToken(request)) {
			respondCSRFError(response);
			return;
		}
		try{
			ReconciliationService service = getReconciliationService(request);
			response.setCharacterEncoding("UTF-8");
	        response.setHeader("Content-Type", "application/json");

	        Writer w = response.getWriter();
	        JsonGenerator writer = ParsingUtilities.mapper.getFactory().createGenerator(w);
	        
	        writer.writeStartObject();
	        writer.writeStringField("code", "ok");
            
	        if(service != null) {
                writer.writeFieldName("service");
                service.writeAsJson(writer);
            }
	        writer.writeEndObject();
	        writer.flush();
	        w.flush();
	        w.close();
		} catch (Exception e) {
			respondException(response, e);
		}
	}

	protected String getIdForString(String name){
		return name.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^-.a-zA-Z0-9]", "").replaceAll("\\-\\-+", "-");
	}
	
	protected ImmutableList<String> asImmutableList(String text){
		List<String> lst = new ArrayList<String>();
		if (StringUtils.isNotBlank(text)) {
			StringTokenizer tokenizer = new StringTokenizer(text," \n");
			while(tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				if(token.trim().isEmpty()){
					continue;
				}
				lst.add(token.trim());
			}
		}
		return ImmutableList.copyOf(lst);
	}

	protected boolean hasValidCSRFTokenAsHeader(HttpServletRequest request) throws ServletException {
		if (request == null) {
			throw new IllegalArgumentException("parameter 'request' should not be null");
		}
		try {
			String token = request.getHeader("X-CSRF-TOKEN");
			return token != null && csrfFactory.validToken(token);
		} catch (Exception e) {
			// ignore
		}
		throw new ServletException("Can't find CSRF token: missing or bad URL parameter");
	}

	protected abstract ReconciliationService getReconciliationService(HttpServletRequest request) throws IOException;

	protected RDFFormat rdfFormat(String format) {
        if (format.equals("TTL")) {
            return RDFFormat.TURTLE;
        } else if (format.equals("RDF/XML")) {
            return RDFFormat.RDFXML;
        } else if (format.equals(".owl")) {
            return RDFFormat.RDFXML;
        } else if (format.equals("N-TRIPLE")) {
            return RDFFormat.NTRIPLES;
        } else if (format.equals("N3")) {
            return RDFFormat.N3;
        }

        return RDFFormat.RDFXML;
    }
}
