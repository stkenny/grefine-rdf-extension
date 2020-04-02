package org.deri.grefine.rdf.vocab;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public class SearchResultItem {
	private String label;
	private String id;
	private String description;
	private String prefix;
	private String localName;

	public SearchResultItem(String id, String prefix, String lname,
			String label, String description) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.prefix = prefix;
		this.localName = lname;
	}

	public String getLabel() {
		return label;
	}

	public String getId() {
		return id;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getDescription() {
		return description;
	}

	public void writeAsSearchResult(JsonGenerator writer) throws IOException {
		writer.writeStartObject();
		writer.writeStringField("id", id);
		writer.writeStringField("name", prefix + ":" + localName);
		writer.writeStringField("description", id + "<br/><em>label: </em>" + label
				+ "<br/><em>description: </em>" + description);
		writer.writeEndObject();
	}

}
