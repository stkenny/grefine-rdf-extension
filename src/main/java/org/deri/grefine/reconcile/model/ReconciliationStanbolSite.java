package org.deri.grefine.reconcile.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reconciliation Stanbol Site 
 * 
 * @author Sergio Fernández <sergio.fernandez@salzburgresearch.at>
 *
 */
public class ReconciliationStanbolSite {
	
	private static final String URI = "uri";
	private static final String NAME = "name";
	private static final String LOCAL = "local";
	private String uri;
	private String name;
	private boolean local;
	
	public ReconciliationStanbolSite() {
		super();
	}

	public ReconciliationStanbolSite(String uri, String name, boolean local) {
		super();
		this.uri = uri;
		this.name = name;
		this.local = local;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReconciliationStanbolSite other = (ReconciliationStanbolSite) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public JsonNode getJSON() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode map = mapper.createObjectNode();
		map.put(URI, this.uri);
		map.put(NAME, this.name);
		map.put(LOCAL, this.local);

		return map;
	}

}
