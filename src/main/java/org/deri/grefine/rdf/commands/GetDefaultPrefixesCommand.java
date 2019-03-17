package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.PrefixExistException;
import org.deri.grefine.rdf.vocab.Vocabulary;
import org.deri.grefine.rdf.vocab.VocabularyImportException;
import org.deri.grefine.rdf.vocab.VocabularyImporter;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;
import org.json.JSONException;
import org.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.Jsonizable;


public class GetDefaultPrefixesCommand extends RdfCommand{

	public GetDefaultPrefixesCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");
        try{
			getDefaultPrefixes(request, response);
        } catch (Exception e) {
            respondException(response, e);
        }
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", "application/json");
		try {
			getDefaultPrefixes(request, response);
		} catch (Exception e) {
			respondException(response, e);
		}
	}

	private void getDefaultPrefixes(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String projectId = request.getParameter("project");
		Collection<Vocabulary> voc = getRdfSchema(request).getPrefixesMap().values();
		for (Vocabulary v : voc) {
			try {
				getRdfContext().getVocabularySearcher().importAndIndexVocabulary(v.getName(), v.getUri(), v.getUri(), projectId, new VocabularyImporter());
			} catch (VocabularyImportException | VocabularyIndexException | PrefixExistException e) {
				logger.error("Error adding default prefix to project: " + e);
			}
		}
		respondJSON(response, new PrefixesList(voc));
	}
	
	public class PrefixesList implements Jsonizable {
		
		@JsonProperty("prefixes")
		public Collection<Vocabulary> prefixes;
		
		@JsonCreator
		public PrefixesList(
				@JsonProperty("prefixes")
				Collection<Vocabulary> prefixes) {
			this.prefixes = prefixes;
		}
		
		public Map<String, Vocabulary> getMap() {
			return prefixes.stream()
					.collect(Collectors.toMap(Vocabulary::getName, Function.identity()));
		}

		@Override
		public void write(JSONWriter writer, Properties options) throws JSONException {
			writer.object();
			writer.key("prefixes");
			writer.array();
			for (Vocabulary v : prefixes) {
				writer.object();
				writer.key("name");
				writer.value(v.getName());
				writer.key("uri");
				writer.value(v.getUri());
				writer.endObject();
			}
			writer.endArray();
			writer.endObject();
		}
	}

}
