package org.deri.grefine.rdf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface CellNode extends Node{
	@JsonProperty("isRowNumberCell")
	boolean isRowNumberCellNode();
	
	@JsonProperty("columnName")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String getColumnName();
}
