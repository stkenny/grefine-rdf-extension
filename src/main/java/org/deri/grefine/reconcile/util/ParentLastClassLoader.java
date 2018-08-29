package org.deri.grefine.reconcile.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ParentLastClassLoader extends ClassLoader {

    private String[] jarFiles; //Paths to the jar files
    private Hashtable classes = new Hashtable(); //used to cache already defined classes

    public ParentLastClassLoader(ClassLoader parent, String[] paths)
    {
        super(parent);
        this.jarFiles = paths;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        System.out.println("Trying to find " + name);
        throw new ClassNotFoundException();
    }

    @Override
    protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException
    {
        System.out.println("Trying to load");
        try
        {
            System.out.println("Loading class in Child : " + className);
            byte classByte[];
            Class result = null;

            //checks in cached classes
            result = (Class) classes.get(className);
            if (result != null) {
                return result;
            }

            for(String jarFile: jarFiles){
                try {
                    JarFile jar = new JarFile(jarFile);
                    JarEntry entry = jar.getJarEntry(className.replace(".","/") + ".class");
                    InputStream is = jar.getInputStream(entry);
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    int nextValue = is.read();
                    while (-1 != nextValue) {
                        byteStream.write(nextValue);
                        nextValue = is.read();
                    }

                    classByte = byteStream.toByteArray();
                    result = defineClass(className, classByte, 0, classByte.length, null);
                    classes.put(className, result);
                } catch (Exception e) {
                    continue;
                }
            }

            result = (Class) classes.get(className);
            if (result != null) {
                return result;
            }
            else{
                throw new ClassNotFoundException("Not found "+ className);
            }
        }
        catch( ClassNotFoundException e ){

            System.out.println("Delegating to parent : " + className);
            // didn't find it, try the parent
            return super.loadClass(className, resolve);
        }
    }
}
