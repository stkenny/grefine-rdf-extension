package org.deri.grefine.rdf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.IOException;

abstract public class ResourceNode implements Node {

    private List<Link> links = new ArrayList<Link>();
    
    @JsonProperty("links")
    public List<Link> getLinks() {
		return links;
	}

	private List<RdfType> rdfTypes = new ArrayList<RdfType>();
    
    public void addLink(Link link) {
        this.links.add(link);
    }

    public void addType(RdfType type) {
        this.rdfTypes.add(type);
    }

    public Link getLink(int index) {
        return this.links.get(index);
    }

    @JsonIgnore
    public int getLinkCount() {
        return this.links.size();
    }

    @JsonProperty("rdfTypes")
    public List<RdfType> getTypes() {
        return this.rdfTypes;
    }

    protected abstract void writeNode(JsonGenerator writer) throws JsonGenerationException, IOException;
    public void write(JsonGenerator writer, Properties options) throws JsonGenerationException, IOException {
        writer.writeStartObject();
        //writer node
        writeNode(writer);
        //write types
        writer.writeFieldName("rdfTypes");
        writer.writeStartArray();
        for(RdfType type:this.getTypes()){
            writer.writeStartObject();
            writer.writeStringField("uri", type.uri);
            writer.writeStringField("curie", type.curie);
            writer.writeEndObject();
        }
        writer.writeEndArray();
        //write links
        writer.writeFieldName("links");
        writer.writeStartArray();
        for(int i=0;i<getLinkCount();i++){
            Link l = getLink(i);
            l.write(writer, options);
        }
        writer.writeEndArray();

        writer.writeEndObject();
    }

    protected void addTypes(Resource[] rs,ValueFactory factory, RepositoryConnection con, URI baseUri) throws RepositoryException {
    	for(Resource r:rs){
    		for(RdfType type:this.getTypes()){
    			Statement stmt = factory.createStatement(r, RDF.TYPE, factory.createURI(Util.resolveUri(baseUri, type.uri)));
    			con.add(stmt);
    		}
    	}
    }
    
    protected Resource[] addLinks(Resource[] rs,URI baseUri,ValueFactory factory,RepositoryConnection con, Project project,Row row,int rowIndex,BNode[] blanks) throws RepositoryException{
   		for(int i=0;i<getLinkCount();i++){
           	Link l = getLink(i);
           	org.eclipse.rdf4j.model.URI p = factory.createURI(Util.resolveUri(baseUri, l.propertyUri));
           	Value[] os = l.target.createNode(baseUri, factory, con, project, row, rowIndex,blanks);
           	if(os!=null){
           		for(Value o:os){
           			for(Resource r:rs){
           				con.add(factory.createStatement(r, p, o));
           			}
           		}
           	}
       	}
        return rs;
    }

    public void setTypes(List<RdfType> types) {
        this.rdfTypes = types;
    }
    
    public Value[] createNode(URI baseUri,ValueFactory factory,RepositoryConnection con, Project project,Row row,int rowIndex,BNode[] blanks) {
        Resource[] r = createResource(baseUri, factory, project, row, rowIndex,blanks);
        if(r==null){
            return null;
        }
        try{
        	addTypes(r, factory,con, baseUri);
        	return addLinks(r,baseUri,factory,con, project,row,rowIndex,blanks);
        }catch(RepositoryException e){
        	throw new RuntimeException(e);
        }
    }
    
    public abstract Resource[] createResource(URI baseUri,ValueFactory factory, Project project,Row row,int rowIndex,BNode[] blanks) ;
    
    public static class RdfType{
        String uri;
		String curie;
        
        @JsonProperty("uri")
        public String getUri() {
			return uri;
		}
        
        @JsonProperty("curie")
        public String getCurie() {
        	return curie;
        }
        
        @JsonCreator
        public RdfType(
        		@JsonProperty("uri")
        		String uri,
        		@JsonProperty("curie")
        		String curie){
            this.uri = uri;
            this.curie = curie;
            
        }
    }
}
