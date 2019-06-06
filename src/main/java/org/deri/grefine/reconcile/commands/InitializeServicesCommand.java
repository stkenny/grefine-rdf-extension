package org.deri.grefine.reconcile.commands;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.deri.grefine.reconcile.GRefineServiceManager;
import org.deri.grefine.reconcile.model.ReconciliationService;

import com.fasterxml.jackson.databind.JsonNode;

import com.google.refine.util.ParsingUtilities;


public class InitializeServicesCommand extends AbstractAddServiceCommand{

	@Override
	protected ReconciliationService getReconciliationService(HttpServletRequest request) {
		try {
			JsonNode arr = ParsingUtilities.evaluateJsonStringToArrayNode(request.getParameter("services"));
			Set<String> urls = new HashSet<String>();
			for(int i=0;i<arr.size();i++){
				urls.add(arr.get(i).asText());
			}
            GRefineServiceManager.singleton.synchronizeServices(urls);
			Set<ReconciliationService> services = GRefineServiceManager.singleton.getServices();

			if (services.isEmpty()) {
			    return null;
            } else {
                return services.iterator().next();
            }

		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize services", e);
		}
	}
}
