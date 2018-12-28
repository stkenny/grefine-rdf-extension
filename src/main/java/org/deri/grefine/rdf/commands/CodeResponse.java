package org.deri.grefine.rdf.commands;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.refine.Jsonizable;

public class CodeResponse implements Jsonizable {
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

	@Override
	public void write(JSONWriter writer, Properties options) throws JSONException {
		writer.object();
        writer.key("code"); writer.value("ok");
        writeOtherFields();
        writer.endObject();
	}

	protected void writeOtherFields() throws JSONException {
		
	}
	
	public static final CodeResponse ok = new CodeResponse("ok");
}
