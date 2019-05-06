package org.deri.grefine.rdf;

import java.util.Properties;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

public class Link {

	@JsonProperty("uri")
    public final String propertyUri;
	@JsonProperty("curie")
    public final String curie;
	@JsonProperty("target")
	@JsonInclude(JsonInclude.Include.NON_NULL)
    public final Node target;

    @JsonCreator  
    public Link(
    		@JsonProperty("uri")
    		String uri,
    		@JsonProperty("curie")
    		String curie,
    		@JsonProperty("target")
    		Node t){
        this.propertyUri = uri;
        this.target = t;
        this.curie = curie;
    }

	public void write(JsonGenerator writer, Properties options) throws JsonGenerationException, IOException{
		writer.writeStartObject();
		writer.writeStringField("uri", propertyUri);
		writer.writeStringField("curie", curie);
		if (target != null) {
			writer.writeFieldName("target");
			target.write(writer, options);
		}
		writer.writeEndObject();
	}
}
