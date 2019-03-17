package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.commands.GetDefaultPrefixesCommand.PrefixesList;
import org.deri.grefine.rdf.vocab.Vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SavePrefixesCommand extends RdfCommand{

	public SavePrefixesCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			Map<String, Vocabulary> prefixesMap = new HashMap<String, Vocabulary>();
			ObjectMapper mapper = new ObjectMapper();
			PrefixesList list = mapper.readValue(request.getParameter("prefixes"), PrefixesList.class);
			getRdfSchema(request).setPrefixesMap(list.getMap());
			
			String projectId = request.getParameter("project");
			getRdfContext().getVocabularySearcher().synchronize(projectId, prefixesMap.keySet());
			
			respondJSON(response, CodeResponse.ok);
		} catch (Exception e) {
            respondException(response, e);
        }
	}
}
