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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.expr.EvalError;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class CellBlankNode extends ResourceNode implements CellNode {

    final private String columnName;
    final boolean isRowNumberCell;
    final private String expression;
    
    @JsonCreator
    public CellBlankNode(
    		@JsonProperty("columnName")
    		String columnName,
    		@JsonProperty("expression")
    		String exp,
    		@JsonProperty("isRowNumberCell")
    		boolean isRowNumberCell){
        this.columnName = columnName;
        this.isRowNumberCell = isRowNumberCell;
        this.expression = exp == null ? "value" : exp;
    }
    
    @Override
    public Resource[] createResource(URI baseUri, ValueFactory factory, Project project,
									 Row row, int rowIndex,BNode[] blanks) {
    	try{
    		Object result = Util.evaluateExpression(project, expression, columnName, row, rowIndex);
    		if(result.getClass()==EvalError.class){
    			return null;
    		}
    		if(result.getClass().isArray()){
    			int lngth = Array.getLength(result);
    			Resource[] bs = new BNode[lngth];
    			for(int i=0;i<lngth;i++){
    				bs[i] = factory.createBNode();
    			}
    			return bs;
    		}
    		return new Resource[]{factory.createBNode()};
    	}catch(Exception e){
    		return null;
    	}
    }

    @Override
    public void writeNode(JsonGenerator writer) throws JsonGenerationException, IOException {
        writer.writeStringField("nodeType", "cell-as-blank");
        writer.writeBooleanField("isRowNumberCell", isRowNumberCell);
        if(columnName!=null){
        	writer.writeStringField("columnName", columnName);
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
	public String getNodeType() {
		return "cell-as-blank";
	}
	
	@JsonProperty("expression")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getExpressionJson() {
		return "value".equals(expression) ? null : expression;
	}
}
