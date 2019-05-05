package org.deri.grefine.rdf.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.Vocabulary;

import com.google.refine.util.ParsingUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SavePrefixesCommand extends RdfCommand{

	public SavePrefixesCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			Map<String, Vocabulary> prefixesMap = new HashMap<String, Vocabulary>();
			ArrayNode prefixesArr = ParsingUtilities.evaluateJsonStringToArrayNode(request.getParameter("prefixes"));
			for(int i =0;i<prefixesArr.size();i++){
				JsonNode prefixObj = prefixesArr.get(i);
				String name = prefixObj.get("name").asText();
				prefixesMap.put(name,new Vocabulary(name, prefixObj.get("uri").asText()));
			}
			getRdfSchema(request).setPrefixesMap(prefixesMap);

			String projectId = request.getParameter("project");
			getRdfContext().getVocabularySearcher().synchronize(projectId, prefixesMap.keySet());
			
			respondJSON(response, CodeResponse.ok);
		} catch (Exception e) {
            respondException(response, e);
        }
	}
}
