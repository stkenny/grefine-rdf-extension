package org.deri.grefine.reconcile.util;

import org.shaded.apache.jena.rdf.model.Model;

public interface RdfUtilities {
	public Model dereferenceUri(String uri);
}
