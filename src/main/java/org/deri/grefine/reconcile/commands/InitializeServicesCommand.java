package org.deri.grefine.reconcile.commands;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


import org.deri.grefine.reconcile.GRefineServiceManager;
import org.deri.grefine.reconcile.model.ReconciliationService;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.refine.util.ParsingUtilities;


public class InitializeServicesCommand extends AbstractAddServiceCommand{

	@Override
	protected ReconciliationService getReconciliationService(HttpServletRequest request) throws JSONException, IOException {
		try {
			JSONArray arr = ParsingUtilities.evaluateJsonStringToArray(request.getParameter("services"));
			Set<String> urls = new HashSet<String>();
			for(int i=0;i<arr.length();i++){
				urls.add(arr.getString(i));
			}
            GRefineServiceManager.singleton.synchronizeServices(urls);
			Set<ReconciliationService> services = GRefineServiceManager.singleton.getServices();

			if (services.isEmpty()) {
			    return null;
            } else {
                return services.iterator().next();
            }

		} catch (JSONException e) {
			throw new RuntimeException("Failed to initialize services", e);
		}
	}
}
