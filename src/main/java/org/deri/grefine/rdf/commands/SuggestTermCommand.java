package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.Util;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.SearchResultItem;
import org.deri.grefine.rdf.vocab.Vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.refine.ProjectManager;
import com.google.refine.model.Project;

public class SuggestTermCommand extends RdfCommand{

	private static Pattern qnamePattern = Pattern.compile("^[_a-zA-Z][-._a-zA-Z0-9]*:([_a-zA-Z][-._a-zA-Z0-9]*)?");
	
	public SuggestTermCommand(ApplicationContext ctxt) {
		super(ctxt);
	}
	
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	//type will hold the project Id. parameters names are defined by the JavaScript library. 
        String projectId = request.getParameter("type");
        
        response.setHeader("Content-Type", "application/json");
        
        JsonGenerator writer = (new ObjectMapper()).getFactory().createGenerator(response.getWriter());
        String type = request.getParameter("type_strict");
        
        String query = request.getParameter("prefix");
        
        try{
            writer.writeStartObject();
            
            writer.writeStringField("prefix", query);
            
            List<SearchResultItem> nodes;
            if(type!=null && type.trim().equals("property")){
                nodes = getRdfContext().getVocabularySearcher().searchProperties(query,projectId);
            }else{
                nodes = getRdfContext().getVocabularySearcher().searchClasses(query,projectId);
            }
            
            if(nodes.size()==0){
            	RdfSchema schema = Util.getProjectSchema(getRdfContext(),getProject(request));
            	nodes = search(schema,query);
            }
            for(SearchResultItem c:nodes){
                c.writeAsSearchResult(writer);
            }
            writer.writeEndArray();
            writer.writeEndObject();
        }catch(Exception e){
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

	@Override
	protected Project getProject(HttpServletRequest request)
			throws ServletException {
    	String projectId = request.getParameter("type");
    	return ProjectManager.singleton.getProject(Long.parseLong(projectId));
	}

	private boolean isPrefixedQName(String s){
    	return qnamePattern.matcher(s).find();
    }
    
    private List<SearchResultItem> search(RdfSchema schema, String query){
    	List<SearchResultItem> result = new ArrayList<SearchResultItem>();
    	
    	if(isPrefixedQName(query)){
    		int index = query.indexOf(":");
    		String prefix = query.substring(0,index);
    		String lPart = query.substring(index + 1);
    		for(Vocabulary v:schema.getPrefixesMap().values()){
    			String name = v.getName();
    			if (name.equals(prefix)){
    				result.add(new SearchResultItem(v.getUri()+lPart, prefix, lPart, "", "Not in the imported vocabulary definition"));
    			}
    		}
    	}else{
    		for(Vocabulary v:schema.getPrefixesMap().values()){
    			String name = v.getName();
    			if (name.startsWith(query)){
    				result.add(new SearchResultItem(v.getUri(), name, "", "", "Not in the imported vocabulary definition"));
    			}
    		}
    	}
    	return result;
    }
}

class Result {
	
	class IdName {
		@JsonProperty("id")
		String id;
		@JsonProperty("name")
		String name;
		
		IdName(String i, String n) {
			id = i;
			name = n;
		}
	}

	@JsonProperty("results")
    private List<IdName> results = new ArrayList<>();
    @JsonProperty("prefix")
    private String prefix;
    
    Result(String p){
        this.prefix = p;
    }
    void addResult(String id, String name){
        results.add(new IdName(id, name));
    }
}
