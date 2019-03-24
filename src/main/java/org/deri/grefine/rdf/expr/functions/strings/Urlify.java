package org.deri.grefine.rdf.expr.functions.strings;

import java.util.Properties;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.ControlFunctionRegistry;
import com.google.refine.grel.Function;

public class Urlify implements Function {

    public Object call(Properties bindings, Object[] args) {
        if(args.length==1){
            String s = args[0].toString();
            if(s.isEmpty()){
            	return new EvalError(ControlFunctionRegistry.getFunctionName(this) + " Cannot urlify empty string");
            }
            s = s.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^-a-zA-Z0-9]", "").replaceAll("\\-\\-+", "-");
            return s;
        }
        return new EvalError(ControlFunctionRegistry.getFunctionName(this) + " expects 1 string");
    }

    @Override
    public String getDescription() {
            return "replaces spaces with underscore";
    }

    @Override
    public String getParams() {
        return "string s";
    }

    @Override
    public String getReturns() {
        return "string";
    }

}
