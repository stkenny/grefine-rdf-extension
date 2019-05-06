package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.app.ApplicationContext;
import com.google.refine.util.ParsingUtilities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SuggestPrefixUriCommand extends RdfCommand{

	public SuggestPrefixUriCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String prefix = request.getParameter("prefix");
		String uri = this.getRdfContext().getPrefixManager().getUri(prefix);
		try{
			response.setCharacterEncoding("UTF-8");
	        response.setHeader("Content-Type", "application/json");
	        Writer w = response.getWriter();
	        JsonGenerator writer = ParsingUtilities.mapper.getFactory().createGenerator(w);
            writer.writeStartObject();
            writer.writeStringField("code", "ok");
            writer.writeStringField("uri", uri);
            writer.writeEndObject();
            writer.flush();
            writer.close();
            w.flush();
            w.close();
		}catch(Exception e){
			respondException(response, e);
		}
	}

}
