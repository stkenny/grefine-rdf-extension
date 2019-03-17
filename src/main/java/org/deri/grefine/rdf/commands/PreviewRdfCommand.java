package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.Node;
import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.exporters.RdfExporter;
import org.deri.grefine.rdf.exporters.RdfExporter.RdfRowVisitor;
import org.deri.grefine.rdf.vocab.Vocabulary;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.refine.Jsonizable;
import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.util.ParsingUtilities;

public class PreviewRdfCommand extends Command {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Project project = getProject(request);
            Engine engine = getEngine(request, project);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");

            String jsonString = request.getParameter("schema");
            ObjectMapper mapper = new ObjectMapper();
            final RdfSchema schema = mapper.readValue(jsonString, RdfSchema.class);

	        StringWriter sw = new StringWriter();
	        RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, sw);
	        RdfRowVisitor visitor = new RdfRowVisitor(schema, w) {
            	final int limit = 10;
            	int _count;
				@Override
				public boolean visit(Project project, int rowIndex, Row row) {
					if(_count>=limit){
		                return true;
		            }
					for(Node root:roots){
						root.createNode(baseUri, factory, con, project, row, rowIndex,blanks);
					}
		            _count +=1;

					try {
						flushStatements();
					} catch (RepositoryException e) {
						e.printStackTrace();
						return true;
					} catch (RDFHandlerException e) {
						e.printStackTrace();
						return true;
					}

		            return false;
				}
			};
			
	        for(Vocabulary v:schema.getPrefixesMap().values()){
		        w.handleNamespace(v.getName(), v.getUri());
	        }
	        RdfExporter.buildModel(project, engine, visitor);

            respondJSON(response, new PreviewResponse(sw.getBuffer().toString()));
        }catch (Exception e) {
            respondException(response, e);
        }
    }
    
    private class PreviewResponse implements Jsonizable {
    	@JsonProperty("v")
    	String value;
    	
    	protected PreviewResponse(String v) {
    		value = v;
    	}

		@Override
		public void write(JSONWriter writer, Properties options) throws JSONException {
			writer.object();
            writer.key("v");
            writer.value(value);
            writer.endObject();
		}
    }
}
