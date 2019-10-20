package org.deri.grefine.rdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import org.deri.grefine.rdf.ResourceNode.RdfType;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.PrefixExistException;
import org.deri.grefine.rdf.vocab.Vocabulary;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

public class RdfSchema implements OverlayModel {

	final static Logger logger = LoggerFactory.getLogger("RdfSchema");
	
    final protected List<Node> _rootNodes = new ArrayList<Node>();
    
    protected URI baseUri;
    /**
     * keys are the short name, values are the full URIs e.g. foaf --> http://xmlns.com/foaf/0.1/
     */
    protected Map<String,Vocabulary> prefixesMap;
    
    @Override
    public void onBeforeSave(Project project) {
    }
    
    @Override
    public void onAfterSave(Project project) {
    }
    
   @Override
    public void dispose(Project project) {
	   /*try {
			ApplicationContext.instance().getVocabularySearcher().deleteProjectVocabularies(String.valueOf(project.id));
		} catch (ParseException e) {
			//log
			logger.error("Unable to delete index for project " + project.id, e);
		} catch (IOException e) {
			//log
			logger.error("Unable to delete index for project " + project.id, e);
		}*/
    }

    public void setBaseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    public RdfSchema(){
    	this.prefixesMap = new HashMap<String, Vocabulary>();
    }
    
    public RdfSchema(ApplicationContext ctxt, Project project) throws VocabularyIndexException, IOException {
        // this value will never be used, just set
        this.baseUri = Util.buildURI("http://localhost:3333/");
        if(this.prefixesMap==null || this.prefixesMap.isEmpty()){
        	this.prefixesMap = clone(ctxt.getPredefinedVocabularyManager().getPredefinedVocabulariesMap());
        	//copy the index of predefined vocabularies
        	//each project will have its own copy of these predefined vocabs to enable delete, update ....
        	ctxt.getVocabularySearcher().addPredefinedVocabulariesToProject(project.id);
        }else{
        	this.prefixesMap = new HashMap<String, Vocabulary>();
        }
    }

    public void addPrefix(String name, String uri) throws PrefixExistException{
    	synchronized(prefixesMap){
    		if(this.prefixesMap.containsKey(name)){
    			throw new PrefixExistException(name + " already defined");
    		}
    		this.prefixesMap.put(name, new Vocabulary(name, uri));
    	}
    }
    
    public void removePrefix(String name){
    	this.prefixesMap.remove(name);
    }
    
    @JsonProperty("baseUri")
    public URI getBaseUri() {
        return baseUri;
    }

    @JsonIgnore
    public Map<String, Vocabulary> getPrefixesMap() {
		return prefixesMap;
	}
    
    @JsonProperty("prefixes")
    public Collection<Vocabulary> getPrefixesList() {
    	return prefixesMap.values();
    }
    
    public void setPrefixesMap(Map<String, Vocabulary> map) {
		this.prefixesMap = map;
	}

    public void setPrefixes(Vocabulary[] vocabularies) {
        for(Vocabulary vocab : vocabularies) {
            this.prefixesMap.put(vocab.getName(), vocab);
        }
    }

    @JsonProperty("rootNodes")
	public List<Node> getRoots() {
        return _rootNodes;
    }

    static public RdfSchema reconstruct(JsonNode o) {
        RdfSchema s = new RdfSchema();
        s.baseUri = Util.buildURI(o.get("baseUri").asText());
        
        JsonNode prefixesArr;
        //for backward compatibility
        if(o.has("prefixes")){
        	prefixesArr = o.get("prefixes");
        }else{
        	prefixesArr = JsonNodeFactory.instance.arrayNode();
        }
        for (int i = 0; i < prefixesArr.size(); i++) {
        	JsonNode prefixObj = prefixesArr.get(i);
        	String name = prefixObj.get("name").asText();
        	s.prefixesMap.put(name, new Vocabulary(name, prefixObj.get("uri").asText()));
        }
        
        JsonNode rootNodes = o.get("rootNodes");
        int count = rootNodes.size();

        for (int i = 0; i < count; i++) {
            JsonNode o2 = rootNodes.get(i);
            Node node = reconstructNode(o2, s);
            if (node != null) {
                s._rootNodes.add(node);
            }
        }

        return s;
    }

    static protected Node reconstructNode(JsonNode o, RdfSchema s) {
        Node node = null;
        String nodeType = o.get("nodeType").asText();
        if (nodeType.startsWith("cell-as-")) {

            JsonNode isRowNumberCellJson = o.get("isRowNumberCell");
        	boolean isRowNumberCell = isRowNumberCellJson == null ? false : isRowNumberCellJson.asBoolean(false);

            String columnName = null;
            if(!isRowNumberCell){
            	columnName = o.get("columnName").asText();
            }
            if ("cell-as-resource".equals(nodeType)) {
                String exp = o.get("expression").asText();
                node = new CellResourceNode(columnName, exp,isRowNumberCell);
                reconstructTypes((CellResourceNode)node,o);
            } else if ("cell-as-literal".equals(nodeType)) {
                String valueType = o.has("valueType") ? Util.getDataType(s.getBaseUri(),o.get("valueType").asText()) : null;
                String lang = o.has("lang") ? o.get("lang").asText() : null;
                //strip off @
                lang = stripAtt(lang);
                String exp;
                if (o.has("expression")){
                	exp = o.get("expression").asText();
                }else{
                	//TODO backward compatibility 
                	exp = "value";
                }
                node = new CellLiteralNode(columnName, exp, valueType, lang,isRowNumberCell);
            } else if ("cell-as-blank".equals(nodeType)) {
            	//TODO blank nodes just accept value as expression
                node = new CellBlankNode(columnName,"value", isRowNumberCell);
                reconstructTypes((CellBlankNode)node, o);
            }
        } else if ("resource".equals(nodeType)) {
            node = new ConstantResourceNode(o.get("value").asText());
            reconstructTypes((ConstantResourceNode)node,o);
        } else if ("literal".equals(nodeType)) {
            String valueType = o.has("valueType") ? Util.getDataType(s.getBaseUri(), o.get("valueType").asText()) : null;
            String lang = o.has("lang") ? o.get("lang").asText() : null;
            //strip off @
            lang = stripAtt(lang);
            node = new ConstantLiteralNode(o.get("value").asText(), valueType,lang);
        } else if ("blank".equals(nodeType)) {
            node = new ConstantBlankNode();
            reconstructTypes((ConstantBlankNode)node,o);
        }

        if (node != null && node instanceof ResourceNode && o.has("links")) {
            ResourceNode node2 = (ResourceNode) node;

            JsonNode links = o.get("links");
            int linkCount = links.size();

            for (int j = 0; j < linkCount; j++) {
                JsonNode oLink = links.get(j);

                node2.addLink(new Link(oLink.get("uri").asText(), oLink.get("curie").asText(),oLink
                        .has("target")
                        && !oLink.get("target").isNull() ? reconstructNode(oLink
                        .get("target"), s) : null));
            }
        }

        return node;
    }

    static private void reconstructTypes(ResourceNode node, JsonNode o) {
    	if (o.has("rdfTypes")) {
    		JsonNode arr = o.get("rdfTypes");
    		List<RdfType> types = new ArrayList<RdfType>();
            for (int i = 0; i < arr.size(); i++) {
                String uri = arr.get(i).get("uri").asText();
                String curie = arr.get(i).get("curie").asText();
                types.add(new RdfType(uri, curie));
            }            
            node.setTypes(types);
        }
    }

    public void write(JsonGenerator writer, Properties options)
            throws JsonGenerationException, IOException {
        writer.writeStartObject();
        if (baseUri == null) {
            baseUri = Util.buildURI("http://localhost:3333/");
        }
        writer.writeStringField("baseUri", baseUri.toString());

        writer.writeFieldName("prefixes");
        writer.writeStartArray();

        for(Vocabulary v: this.prefixesMap.values()){
            writer.writeStartObject();
            writer.writeStringField("name", v.getName());
            writer.writeStringField("uri", v.getUri());
            writer.writeEndObject();
        }
        writer.writeEndArray();

        writer.writeFieldName("rootNodes");
        writer.writeStartArray();
        for (Node node : _rootNodes) {
            node.write(writer, options);
        }

        writer.writeEndArray();
        writer.writeEndObject();

        writer.flush();
        writer.close();
    }

    static public RdfSchema load(Project project, JsonNode obj) throws Exception {
        return reconstruct(obj);
    }
    
    private Map<String,Vocabulary> clone(Map<String,Vocabulary> original){
    	Map<String,Vocabulary> copy = new HashMap<String, Vocabulary>();
    	for(Entry<String, Vocabulary> entry : original.entrySet()){
    		copy.put(entry.getKey(), new Vocabulary(entry.getValue().getName(),entry.getValue().getUri()));
    	}
    	
    	return copy;
    }
    
    private static String stripAtt(String s){
    	if(s==null){
    		return s;
    	}
    	if(s.startsWith("@")){
    		return s.substring(1);
    	}
    	return s;
    }
}