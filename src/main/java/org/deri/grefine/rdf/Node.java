package org.deri.grefine.rdf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.Properties;
import java.io.IOException;

@JsonTypeInfo(
        use=JsonTypeInfo.Id.CUSTOM,
        include=JsonTypeInfo.As.PROPERTY,
        property="nodeType")
@JsonTypeIdResolver(NodeResolver.class)
public interface Node {
    Value[] createNode(URI baseUri, ValueFactory factory, RepositoryConnection con, Project project, Row row, int rowIndex, BNode[] blanks);
    
    @JsonProperty("nodeType")
    public String getNodeType();
    public void write(JsonGenerator writer, Properties options) throws JsonGenerationException, IOException;
}
