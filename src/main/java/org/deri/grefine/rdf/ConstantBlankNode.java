package org.deri.grefine.rdf;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONWriter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class ConstantBlankNode extends ResourceNode{

	private BNode bnode = null;
	
	@JsonCreator
    ConstantBlankNode(){
    }
    
    @Override
    public Resource[] createResource(URI baseUri, ValueFactory factory, Project project,
            Row row, int rowIndex,BNode[] blanks) {
    	if (bnode == null) {
    		bnode = factory.createBNode();
    	}
        return new BNode[]{bnode};
    }

	@Override
	protected void writeNode(JSONWriter writer) throws JSONException {
		writer.key("nodeType"); writer.value("blank");
	}

	@Override
	public String getNodeType() {
		return "blank";
	}

}
