package org.deri.grefine.rdf;

import java.net.URI;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
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
	protected void writeNode(JsonGenerator writer) throws JsonGenerationException, IOException {
		writer.writeStringField("nodeType", "blank");
	}

	@Override
	public String getNodeType() {
		return "blank";
	}

}
