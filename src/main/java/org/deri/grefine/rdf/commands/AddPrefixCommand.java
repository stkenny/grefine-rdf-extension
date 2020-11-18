package org.deri.grefine.rdf.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.PrefixExistException;
import org.deri.grefine.rdf.vocab.VocabularyImportException;
import org.deri.grefine.rdf.vocab.VocabularyImporter;

public class AddPrefixCommand extends RdfCommand{

	public AddPrefixCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!hasValidCSRFToken(request)) {
			respondCSRFError(response);
			return;
		}
		String name = request.getParameter("name").trim();
        String uri = request.getParameter("uri").trim();
        String projectId = request.getParameter("project");
        String fetchOption = request.getParameter("fetch");
        Boolean forceImport = Boolean.valueOf(request.getParameter("force-import"));
        try {
        	getRdfSchema(request).addPrefix(name, uri);
			if(fetchOption.equals("web")){
        		String fetchUrl = request.getParameter("fetch-url");
        		if(fetchUrl==null || fetchOption.trim().isEmpty()){
        			fetchUrl = uri;
        		}
        		getRdfContext().getVocabularySearcher().importAndIndexVocabulary(name, uri, fetchUrl, projectId, new VocabularyImporter());
			}
			respondJSON(response, CodeResponse.ok);
        } catch (PrefixExistException e) {
			getRdfSchema(request).removePrefix(name);
			respondException(response, e);
        } catch (VocabularyImportException e) {
            getRdfSchema(request).removePrefix(name);
			respondException(response, e);
        } catch (Exception e){
            respondException(response, e);
        }
    }
}
