package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.operations.SaveRdfSchemaOperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.process.Process;

public class SaveRdfSchemaCommand extends RdfCommand{

    public SaveRdfSchemaCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            Project project = getProject(request);
            
            String jsonString = request.getParameter("schema");
            ObjectMapper mapper = new ObjectMapper();
            RdfSchema schema = mapper.readValue(jsonString, RdfSchema.class);
            
            AbstractOperation op = new SaveRdfSchemaOperation(schema);
            Process process = op.createProcess(project, new Properties());
            
            performProcessAndRespond(request, response, project, process);
       
            
        } catch (Exception e) {
            respondException(response, e);
        }
    }
}
