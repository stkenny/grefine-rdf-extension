package org.deri.grefine.rdf.vocab;

import org.json.JSONException;
import org.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	
    public void write(JSONWriter writer)throws JSONException {
        writer.object();
        
        writer.key("name"); writer.value(name);
        writer.key("uri"); writer.value(uri);
        
        writer.endObject();
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
