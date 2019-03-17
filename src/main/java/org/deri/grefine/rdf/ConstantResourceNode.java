package org.deri.grefine.rdf;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONWriter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class ConstantResourceNode extends ResourceNode{

    private String uri;

    @JsonProperty("value")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    @JsonCreator
    public ConstantResourceNode(
    		@JsonProperty("value")
    		String uri){
        this.uri = uri;
    }

    @Override
    public Resource[] createResource(URI baseUri, ValueFactory factory, Project project,
            Row row, int rowIndex,BNode[] blanks) {
        if(this.uri!=null & this.uri.length()>0){
            Resource r =  factory.createURI(Util.resolveUri(baseUri, this.uri));
            return new Resource[]{r};
        }else{
            return null;
        }
    }

	@Override
	protected void writeNode(JSONWriter writer) throws JSONException {
		writer.key("nodeType"); writer.value("resource");
        writer.key("value"); writer.value(uri);	
	}

	@Override
	public String getNodeType() {
		return "resource";
	}

}
