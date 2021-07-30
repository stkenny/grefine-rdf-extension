package org.deri.grefine.rdf;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.io.IOException;

public class ConstantLiteralNode implements Node{

    private String valueType;
    private String lang;
    private String value;
    
    @JsonCreator
    public ConstantLiteralNode(
    		@JsonProperty("value")
    		String val,
    		@JsonProperty("valueType")
    		String type,
    		@JsonProperty("lang")
    		String l){
        this.value = val;
        this.valueType = type;
        this.lang = l;
    }
    
    @JsonProperty("valueType")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getValueType() {
        return valueType;
    }


    public void setValueType(String valueType) {
        this.valueType = valueType;
    }


    @JsonProperty("lang")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLang() {
        return lang;
    }


    public void setLang(String lang) {
        this.lang = lang;
    }


    @JsonProperty("value")
    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }

    @Override
	public Value[] createNode(URI baseUri, ValueFactory factory, RepositoryConnection con, Project project,
            Row row, int rowIndex,BNode[] blanks) {
        if(this.value!=null && this.value.length()>0){
            Literal l ;
            if(this.valueType != null){
            	//TODO handle exception when valueType is not a valid URI
                l = factory.createLiteral(this.value, factory.createURI(valueType));
            }else{
            	if(this.lang!=null){
            		l = factory.createLiteral(this.value, lang);
            	}else{
            		l = factory.createLiteral(this.value);
            	}
            }
            return new Literal[]{l};
        }else{
            return null;
        }
    }

	@Override
	public String getNodeType() {
		return "literal";
	}

	@Override
    public void write(JsonGenerator writer, Properties options) throws JsonGenerationException, IOException {
        writer.writeStartObject();
        writer.writeStringField("nodeType", "literal");
        writer.writeStringField("value", value);
        if(valueType!=null){
            writer.writeStringField("valueType", valueType);
        }
        if(lang!=null){
            writer.writeStringField("lang", lang);
        }
        writer.writeEndObject();
    }



}
