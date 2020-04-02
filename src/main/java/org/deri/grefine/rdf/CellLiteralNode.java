package org.deri.grefine.rdf;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.Properties;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.expr.EvalError;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class CellLiteralNode implements CellNode{

	final private String valueType;
    final private String lang;
    final private String columnName;
    final boolean isRowNumberCell;
    final private String expression;
    
    @JsonProperty("valueType")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getValueType() {
        return valueType;
    }
    
    @JsonProperty("lang")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLang() {
        return lang;
    }
    
    @JsonProperty("expression")
    public String getExpression() {
    	return expression;
    }
    
    @JsonCreator
    public CellLiteralNode(
    		@JsonProperty("columnName")
    		String columnName,
    		@JsonProperty("expression")
    		String exp,
    		@JsonProperty("valueType")
    		String valueType,
    		@JsonProperty("lang")
    		String lang,
    		@JsonProperty("isRowNumberCell")
    		boolean isRowNumberCell){
    	this.columnName = columnName;
        this.lang = lang;
        this.valueType = valueType;
        this.isRowNumberCell = isRowNumberCell;
        this.expression = exp;
    }
    @Override
    public Value[] createNode(URI baseUri, ValueFactory factory, RepositoryConnection con, Project project,
            Row row, int rowIndex,BNode[] blanks) {
        String[] val = null;

        	
        try{
            Object result = Util.evaluateExpression(project, expression, columnName, row, rowIndex);
            
            if(result.getClass()==EvalError.class){
            	return null;
            }
            if(result.getClass().isArray()){
            	int lngth = Array.getLength(result);
            	val = new String[lngth];
            	for(int i=0;i<lngth;i++){
            		val[i] = Array.get(result,i).toString();
            	}
            }else if(result.toString().length()>0){
            	val = new String[1];
            	val[0] = result.toString();
            }
    	}catch(Exception e){
    		//an empty cell might result in an exception out of evaluating URI expression... so it is intended to eat the exception
    		val = null;
    	}   
        
        if(val!=null && val.length>0){
        	Literal[] ls = new Literal[val.length];
        	for(int i=0;i<val.length;i++){
        		Literal l;
            	if(this.valueType!=null){
                	l = factory.createLiteral(val[i], factory.createURI(valueType));
            	}else{
            		if(this.lang!=null){
            			l = factory.createLiteral(val[i],lang);
            		}else{
            			l = factory.createLiteral(val[i]);
            		}
            	}
            	ls[i] = l;
        	}
            return ls;
        }else{
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
	public String getNodeType() {
		return "cell-as-literal";
	}

	@Override
	public void write(JsonGenerator writer, Properties options)
			throws JsonGenerationException, IOException {
		writer.writeStartObject();
		writer.writeStringField("nodeType", "cell-as-literal");
		writer.writeStringField("expression", expression);
		writer.writeBooleanField("isRowNumberCell", isRowNumberCell);
		if(valueType!=null){
			writer.writeStringField("valueType", valueType);
		}
		if(lang!=null){
			writer.writeStringField("lang", lang);
		}
		if(columnName!=null){
			writer.writeStringField("columnName", columnName);
		}
		writer.writeEndObject();
	}

}
