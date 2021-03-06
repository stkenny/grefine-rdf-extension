package org.deri.grefine.rdf.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.Util;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;

import com.google.refine.commands.Command;
import com.google.refine.model.Project;

public abstract class RdfCommand extends Command {

	private ApplicationContext rdfContext;
	
	public RdfCommand(ApplicationContext ctxt){
		super();
		this.rdfContext = ctxt;
	}

	public ApplicationContext getRdfContext() {
		return rdfContext;
	}

	public RdfSchema getRdfSchema(HttpServletRequest request) throws ServletException{
       try {
            Project project = getProject(request);
		    return Util.getProjectSchema(getRdfContext(), project);
		} catch (VocabularyIndexException e) {
			throw new ServletException("Unable to create index for RDF schema",e);
		} catch (IOException e) {
            throw new ServletException("Unable to create index for RDF schema",e);
		}
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
}
