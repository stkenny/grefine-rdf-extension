package org.deri.grefine.rdf;

import java.lang.reflect.Array;
import java.net.URI;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.expr.EvalError;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class CellResourceNode extends ResourceNode implements CellNode{

    final private String uriExpression;
    final private String columnName;
    final private boolean isRowNumberCell; 
    
    @JsonProperty("expression")
    public String getUriExpression() {
        return uriExpression;
    }

    @JsonCreator
    public CellResourceNode(
    		@JsonProperty("columnName")
    		String columnName,
    		@JsonProperty("expression")
    		String exp,
    		@JsonProperty("isRowNumberCell")
    		boolean isRowNumberCell) {
    	this.columnName = columnName;
        this.uriExpression = exp;
        this.isRowNumberCell = isRowNumberCell;
    }

    @Override
    public Resource[] createResource(URI baseUri, ValueFactory factory, Project project, Row row, int rowIndex, BNode[] blanks) {
        try{
        	Object result = Util.evaluateExpression(project, uriExpression, columnName, row, rowIndex);
            if(result.getClass()==EvalError.class){
            	return null;
            }
            if(result.getClass().isArray()){
            	int length = Array.getLength(result);
            	Resource[] rs = new org.eclipse.rdf4j.model.URI[length];
            	for(int i=0;i<length;i++){
            		String uri = Util.resolveUri(baseUri,  Array.get(result, i).toString());
            		rs[i] = factory.createURI(uri);
            	}
            	return rs;
            }
            if(result.toString().length()>0){
            	String uri = Util.resolveUri(baseUri, result.toString());
                return new Resource[] {factory.createURI(uri)};
            }else{
                return null;
            }
        }catch(Exception e){
            //an empty cell might result in an exception out of evaluating URI expression... so it is intended to eat the exception
            return null;
            
            
        }
        
    }

	@Override
	public boolean isRowNumberCellNode() {
		return isRowNumberCell;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	protected void writeNode(JsonGenerator writer) throws JsonGenerationException, IOException {
		writer.writeStringField("nodeType", "cell-as-resource");
        writer.writeStringField("expression", uriExpression);
        writer.writeBooleanField("isRowNumberCell", isRowNumberCell);
        if(columnName!=null){
        	writer.writeStringField("columnName", columnName);
        }
		
	}

	@Override
	public String getNodeType() {
		return "cell-as-resource";
	}
}
