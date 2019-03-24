package org.deri.grefine.rdf.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeResponse {
	protected String code;
	
	public CodeResponse() {
		this.code = "ok";
	}
	
	public CodeResponse(String code) {
		this.code = code;
	}

	@JsonProperty
	public String getCode() {
		return code;
	}

	protected void writeOtherFields()  {}
	
	public static final CodeResponse ok = new CodeResponse("ok");
}
