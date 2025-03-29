package org.deri.grefine.rdf;

import com.google.refine.expr.Evaluable;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.expr.MetaParser;
import com.google.refine.grel.Parser;
import com.google.refine.expr.ParsingException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.expr.util.RdfExpressionUtil;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Properties;

public class Util {

	private static final String XSD_INT_URI = "http://www.w3.org/2001/XMLSchema#int";
	private static final String XSD_DOUBLE_URI = "http://www.w3.org/2001/XMLSchema#double";
	private static final String XSD_DATE_URI = "http://www.w3.org/2001/XMLSchema#date";

	private static final String[] PREFIXES = { "http://", "https://", "file://", "ftp://" };

	public static String resolveUri(URI base, String rel) {
		try {
			URI relUri = new URI(rel);
			if (relUri.isAbsolute()) {
			    return rel;
			}
		} catch (URISyntaxException e) {
			String testRel = rel.toLowerCase();
			if (testRel.matches("(" + PREFIXES[0] + "|"
					+ PREFIXES[1] + "|"
					+ PREFIXES[2] + "|"
					+ PREFIXES[3] + ").*")) {
				return "error:" + e.toString();
			}
		}

		String res;
		try{
			res = resolveRelativeURI(base, rel);
			new URI(res);
			return res;
		} catch(Exception ex){
			//try encoding
			try {
				if (!rel.startsWith("/")) {
					rel = "/" + rel;
				}
				URI encodedRel = new URI(
						base.getScheme(),
						base.getUserInfo(),
						base.getHost(),
						base.getPort(),
						rel,
						null,
						null);

				return resolveRelativeURI(base, encodedRel.getRawPath());
			} catch (URISyntaxException e) {
				return "error:" + e.toString();
			}
		}
	}

	private static String resolveRelativeURI(URI base, String rel){
		if (base.getFragment() != null) {
			return base + rel;
		}
		return base.resolve(rel).toString();
	}
	
	public static String getDataType(URI base,String s) {
		if (s == null) {
			return null;
		}
		if (s.equals(XSD_INT_URI)) {
			return XSD_INT_URI;
		}
		if (s.equals(XSD_DOUBLE_URI)) {
			return XSD_DOUBLE_URI;
		}
		if (s.equals(XSD_DATE_URI)) {
			return XSD_DATE_URI;
		}
		return resolveUri(base,s);
	}

	public static RdfSchema getProjectSchema(ApplicationContext ctxt, Project project) throws VocabularyIndexException, IOException {
		synchronized (project) {
			RdfSchema rdfSchema = (RdfSchema) project.overlayModels
					.get("rdfSchema");
			if (rdfSchema == null) {
				rdfSchema = new RdfSchema(ctxt,project);

				project.overlayModels.put("rdfSchema", rdfSchema);
				project.getMetadata().updateModified();
			}
            return rdfSchema;
		}
	}

	public static URI buildURI(String uri) {
		try {
			URI baseUri = new URI(uri);
			return baseUri;
		} catch (URISyntaxException e) {
			throw new RuntimeException("malformed Base URI " + uri, e);
		}
	}
	
	public static Object evaluateExpression(Project project, String expression, String columnName, Row row, int rowIndex) throws ParsingException {
		Properties bindings = ExpressionUtils.createBindings(project);
		MetaParser.registerLanguageParser("grel", "GREL", Parser.grelParser, "value");
		Evaluable eval = MetaParser.parse(expression);
                int cellIndex = (columnName==null||columnName.equals(""))?-1:project.columnModel.getColumnByName(columnName).getCellIndex();
        
                return RdfExpressionUtil.evaluate(eval, bindings, row, rowIndex, columnName, cellIndex);
	}
}
