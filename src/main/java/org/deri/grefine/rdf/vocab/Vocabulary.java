package org.deri.grefine.rdf.vocab;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class Vocabulary {
	private String name;
	private String uri;

	@JsonCreator
    public Vocabulary(@JsonProperty("name") String name, @JsonProperty("uri") String uri){
    	this.name = name;
    	this.uri = uri;
    }
    
    @JsonProperty("name")
	public String getName() {
		return name;
	}
    
    @JsonProperty("uri")
	public String getUri() {
		return uri;
	}
	
    public void write(JsonGenerator writer)throws JsonGenerationException, IOException {
        writer.writeStartObject();
        
        writer.writeStringField("name", name);
        writer.writeStringField("uri", uri);
        
        writer.writeEndObject();
    }


	@Override
	public int hashCode() {
		return name.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if(obj.getClass().equals(this.getClass())){
			Vocabulary v2 = (Vocabulary) obj;
			return name.equals(v2.getName());
		}
		return false;
	}
    
    

}
