package org.deri.grefine.reconcile.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.grefine.reconcile.GRefineServiceManager;
import org.deri.grefine.reconcile.util.ParentLastClassLoader;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.RefineServlet;
import com.google.refine.commands.Command;

public class InitializationCommand extends Command {

    final static Logger logger = LoggerFactory.getLogger("rdf_reconcile_initializer");

    @Override
    public void init(RefineServlet servlet) {
        URLClassLoader currentLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] urls = currentLoader.getURLs();
        ArrayList<String> paths = new ArrayList<>();
        for (URL url : urls) {
            if (url.toString().contains("rdf-extension")) {
                paths.add(url.toString());
                System.out.println(url);
            }
        }
        ClassLoader loader = new ParentLastClassLoader(Thread.currentThread().getContextClassLoader(), paths.toArray(new String[0]));
        try {
            File workingDir = servlet.getCacheDir("rdfExtension/reconcile");

            Class clas = loader.loadClass("org.deri.grefine.reconcile.GRefineServiceManager");
            Method initialize = clas.getMethod("initialize", new Class[] { File.class });
            initialize.invoke(null, new Object[] { workingDir });
        } catch (JSONException e) {
            logger.error("Failed to initialize the extension. ", e);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to initialize the extension. ", e);
        } catch (IllegalAccessException e) {
            logger.error("Failed to initialize the extension. ", e);
        } catch (InvocationTargetException e) {
            logger.error("Failed to initialize the extension. ", e);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to initialize the extension. ", e);
        }
    }

    public String getClassLocation(Class clazz) {
        if (clazz == null) {
            return null;
        }

        try {
            ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            URL location = codeSource.getLocation();
            return location.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return "Not found";
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        throw new UnsupportedOperationException("This command is not meant to be called. It is just necessary for initialization");
    }
}
