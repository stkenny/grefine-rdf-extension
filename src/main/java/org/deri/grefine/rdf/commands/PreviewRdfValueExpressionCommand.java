package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.Util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import com.google.refine.commands.expr.PreviewExpressionCommand;
import com.google.refine.expr.EvalError;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.expr.ParsingException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.util.ParsingUtilities;

public class PreviewRdfValueExpressionCommand extends PreviewExpressionCommand{

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
            Project project = getProject(request);
            
            String columnName = request.getParameter("columnName");
            String uri = request.getParameter("isUri");
            boolean isUri = uri!=null && uri.equals("1") ? true:false;
            
            String expression = request.getParameter("expression");
            String rowIndicesString = request.getParameter("rowIndices");
            if (rowIndicesString == null) {
                respond(response, "{ \"code\" : \"error\", \"message\" : \"No row indices specified\" }");
                return;
            }
            
            String baseUri = request.getParameter("baseUri");
            URI base;
            try{
            	base = new URI(baseUri);
            }catch(URISyntaxException ex){
            	respond(response, "{ \"code\" : \"error\", \"message\" : \"Invalid Base URI\" }");
                return;
            }
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
            
            JsonNode rowIndices = ParsingUtilities.evaluateJsonStringToArrayNode(rowIndicesString);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonGenerator writer = mapper.getFactory().createGenerator(response.getWriter());
            if(isUri){
            	respondUriPreview(project, writer, rowIndices, expression, columnName, base);
            }else{
            	respondLiteralPreview(project, writer, rowIndices, expression, columnName);
            }
        } catch (Exception e) {
            respondException(response, e);
        }
	}
	
	private void respondUriPreview(Project project, JsonGenerator writer, JsonNode rowIndices, String expression, String columnName, URI base) throws IOException{
		int length = rowIndices.size();
        
        writer.writeStartObject();
        
        try {
            writer.writeArrayFieldStart("results");
            String[] absolutes = new String[length];
            for (int i = 0; i < length; i++) {
                Object result = null;
                absolutes[i] = null;
                int rowIndex = rowIndices.get(i).asInt();
                if (rowIndex >= 0 && rowIndex < project.rows.size()) {
                    Row row = project.rows.get(rowIndex);
                    result = Util.evaluateExpression(project, expression, columnName, row, rowIndex);
                }
                
                if (result == null) {
                    writer.writeNull();
                } else if (ExpressionUtils.isError(result)) {
                    writer.writeStartObject();
                    writer.writeStringField("message", ((EvalError) result).message);
                    writer.writeEndObject();
                } else {
                	StringBuffer sb = new StringBuffer();
                    writeValue(sb, result, false);
                    writer.writeString(sb.toString());

                    //prepare absolute value                    
                	if (result.getClass().isArray()) {
                		int lngth = Array.getLength(result);
                		StringBuilder resolvedUrisVal = new StringBuilder("[");
                		for(int k=0;k<lngth;k++){
                			resolvedUrisVal.append(Util.resolveUri(base, Array.get(result, k).toString()));
                			if(k<lngth-1){
                				resolvedUrisVal.append(",");
                			}
                		}
                		resolvedUrisVal.append("]");
                		absolutes[i] = resolvedUrisVal.toString();
                	} else {
                        absolutes[i] = Util.resolveUri(base, sb.toString());
                	}
                }
            }
            writer.writeEndArray();
            
            //writing the absolutes
            writer.writeArrayFieldStart("absolutes");
            for (int i = 0; i < length; i++) {
                String absolute = absolutes[i];
                if (absolute.startsWith("error:")) {
                    writer.writeStartObject();
                    writer.writeStringField("message", absolute.substring(6));
                    writer.writeEndObject();
                } else {
                    writer.writeString(absolute);
                }
            }
            writer.writeEndArray();
            writer.writeStringField("code", "ok");
        } catch (ParsingException e) {
        	writer.writeEndArray();
            writer.writeStringField("code", "error");
            writer.writeStringField("type", "parser");
            writer.writeStringField("message", e.getMessage());
        } catch (Exception e) {
        	writer.writeEndArray();
            writer.writeStringField("code", "error");
            writer.writeStringField("type", "other");
            writer.writeStringField("message", e.getMessage());
        }
        
        writer.writeEndObject();

        writer.flush();
        writer.close();
	}
	
	
	private void respondLiteralPreview(Project project, JsonGenerator writer, JsonNode rowIndices, String expression, String columnName) throws IOException{
		int length = rowIndices.size();
        
        writer.writeStartObject();
        
        try {
            writer.writeArrayFieldStart("results");
            for (int i = 0; i < length; i++) {
                Object result = null;
                int rowIndex = rowIndices.get(i).asInt();
                if (rowIndex >= 0 && rowIndex < project.rows.size()) {
                    Row row = project.rows.get(rowIndex);
                    result = Util.evaluateExpression(project, expression, columnName, row, rowIndex); 
                }
                
                if (result == null) {
                    writer.writeNull();
                } else if (ExpressionUtils.isError(result)) {
                    writer.writeStartObject();
                    writer.writeStringField("message", ((EvalError) result).message);
                    writer.writeEndObject();
                } else {
                    StringBuffer sb = new StringBuffer();
                    writeValue(sb, result, false);
                    writer.writeString(sb.toString());
                }
            }
            writer.writeEndArray();
            
            writer.writeStringField("code", "ok");
        } catch (ParsingException e) {
            writer.writeEndArray();
            writer.writeStringField("code", "error");
            writer.writeStringField("type", "parser");
            writer.writeStringField("message", e.getMessage());
        } catch (Exception e) {
        	writer.writeEndArray();
            writer.writeStringField("code", "error");
            writer.writeStringField("type", "other");
            writer.writeStringField("message", e.getMessage());
        }
        
        writer.writeEndObject();
        writer.flush();
        writer.close();
	}
}
