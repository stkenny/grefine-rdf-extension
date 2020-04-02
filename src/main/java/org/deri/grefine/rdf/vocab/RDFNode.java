package org.deri.grefine.rdf.vocab;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import java.util.Properties;
import java.io.IOException;

public class RDFNode {
    private String localPart;
    private String description;
    private String URI;
    private String label;
    private String vocabularyPrefix;
    private String vocabularyUri;
    
    public String getVocabularyUri() {
        return vocabularyUri;
    }
    public void setVocabularyUri(String vocabularyUri) {
        this.vocabularyUri = vocabularyUri;
    }
    public String getVocabularyPrefix() {
        return vocabularyPrefix;
    }
    public void setVocabularyPrefix(String vocabularyPrefix) {
        this.vocabularyPrefix = vocabularyPrefix;
    }
    public String getLocalPart() {
        return localPart;
    }
    public void setLocalPart(String l) {
        this.localPart = l;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getURI() {
        return URI;
    }
    public void setURI(String uRI) {
        URI = uRI;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public RDFNode(){
        
    }
    public RDFNode(String uRI, String label, String description, 
            String prefix,String vocabularyUri) {
    	this(uRI, label, description,prefix, vocabularyUri, "");
        this.localPart = extractlocalPart();
    }
    
    //TODO - refactor this constructor... it has many arguments and of the same type. Use helper class? builder?
    public RDFNode(String uRI, String label, String description, 
            String prefix, String vocabularyUri, String lPart) {
        this.description = description;
        URI = uRI;
        this.label = label;
        this.vocabularyPrefix = prefix;
        this.localPart = lPart;
        this.vocabularyUri = vocabularyUri;
    }
    
    private String extractlocalPart(){
        String l;
        if(this.URI==null){
            return null;
        }
        if(this.URI.indexOf("#")!=-1){
            l = this.URI.substring(this.URI.indexOf("#")+1);
        }else{
            l = this.URI.substring(this.URI.lastIndexOf("/")+1);
        }
        return l;
    }
    
    
    public String getType(){
    	throw new UnsupportedOperationException(); 
    }
    
    public void write(JsonGenerator writer, Properties options)
            throws JsonGenerationException, IOException {
        writer.writeStartObject();

        writer.writeStringField("type", this.getType());
        writer.writeStringField("prefix", vocabularyPrefix);
        writer.writeStringField("localPart", this.localPart);
        writer.writeStringField("label", label);
        writer.writeStringField("description", description);
        writer.writeStringField("URI", URI.toString());
        writer.writeEndObject();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof RDFNode)) return false;
        RDFNode n = (RDFNode) obj;
        if(n.getURI()==null || this.URI==null){
            return false;
        }
        return this.URI.equals(n.getURI());
    }
    @Override
    public int hashCode() {
        return this.URI.hashCode();
    }
  
}
