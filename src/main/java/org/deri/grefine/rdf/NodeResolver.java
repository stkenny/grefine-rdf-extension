package org.deri.grefine.rdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NodeResolver extends TypeIdResolverBase  {
    protected TypeFactory factory = TypeFactory.defaultInstance();
    
    @SuppressWarnings("rawtypes")
	private static Map<String, Class> registry = new HashMap<>();

    static {
    	
    	registry.put("literal", ConstantLiteralNode.class);
    	registry.put("blank", ConstantBlankNode.class);
    	registry.put("resource", ConstantResourceNode.class);
    	registry.put("cell-as-literal", CellLiteralNode.class);
    	registry.put("cell-as-blank", CellBlankNode.class);
    	registry.put("cell-as-resource", CellResourceNode.class);
    	
    }
    
    @SuppressWarnings("rawtypes")
	public void registerNodeType(String nodeType, Class klass) {
    	registry.put(nodeType, klass);
    }

    @Override
    public Id getMechanism() {
        return Id.NAME;
    }

    @Override
    public String idFromValue(Object instance) {
        return ((Node)instance).getNodeType();
    }

    @Override
    public String idFromValueAndType(Object instance, Class<?> type) {
        return ((Node) instance).getNodeType();
    }
    
    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        return factory.constructSimpleType(registry.get(id), new JavaType[0]);
    }

}
