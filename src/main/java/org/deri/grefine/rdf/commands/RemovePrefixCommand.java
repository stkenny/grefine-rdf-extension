package org.deri.grefine.rdf.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.app.ApplicationContext;

public class RemovePrefixCommand extends RdfCommand{

	public RemovePrefixCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		String projectId = request.getParameter("project");
		getRdfSchema(request).removePrefix(name);
		
		getRdfContext().getVocabularySearcher().deleteTermsOfVocab(name, projectId);
		try{
			respondJSON(response, CodeResponse.ok);
		} catch (IOException e) {
			respondException(response, e);
		} 
	}
}
