package org.deri.grefine.rdf;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.Jsonizable;

public class Link implements Jsonizable{

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
    
    public void write(JSONWriter writer, Properties options)throws  JSONException{

        writer.object();
        writer.key("uri"); writer.value(propertyUri);
        writer.key("curie"); writer.value(curie);
        if (target != null) {
            writer.key("target");
            target.write(writer, options);
        }
        writer.endObject();
    }
}
