package de.gmcs.circuitbreaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CircuitBreakerInitializer {

    private String root;

    public void setRoot(String root) {
        this.root = root;
    }

    public void scan() throws IOException {
        for (URL url : getRootUrls()) {
            File f = new File (url.getPath());
            if (f.isDirectory()) {
                visitFile(f);
            } else {
                visitJar(url);
            }
        }
    }

    private List<URL> getRootUrls() throws IOException {
        List<URL> result = new ArrayList<> ();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                Enumeration<URL> urls = ((URLClassLoader) cl).getResources(root);
                while(urls.hasMoreElements()) {
                    result.add(urls.nextElement());
                }
            }
            cl = cl.getParent();
        }
        return result;
    }

    private void visitFile (File f) throws IOException {
        if (f.isDirectory ()) {
            final File[] children = f.listFiles ();
            if (children != null) {
                for (File child: children) {
                    visitFile (child);
                }
            }
        } else if (f.getName().endsWith(".class")) {
            try (FileInputStream in = new FileInputStream (f)) {
                handleClass(in);
            }
        }
    }

    private void visitJar(URL url) throws IOException {
        System.out.println("url: " + url);
        try (InputStream urlIn = url.openStream(); JarInputStream jarIn = new JarInputStream(urlIn)) {
            JarEntry entry;
            while ((entry = jarIn.getNextJarEntry()) != null) {
                if (entry.getName().endsWith (".class")) {
                    handleClass(jarIn);
                }
            }
        }
    }

    private void handleClass(InputStream in) throws IOException {
        MyClassVisitor cv = new MyClassVisitor ();
        new ClassReader (in).accept (cv, 0);
    }


    private static class MyClassVisitor extends ClassVisitor {

        private String currentClassName;

        MyClassVisitor() {
            super(Opcodes.ASM5);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        	  this.currentClassName = name;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MyMethodVisitor(currentClassName);
        }
    }


    private static class MyMethodVisitor extends MethodVisitor {

        private String currentClassName;

        MyMethodVisitor(String currentClassName) {
            super(Opcodes.ASM5);
            this.currentClassName = currentClassName;
        }
/*
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }*/
    }
}
