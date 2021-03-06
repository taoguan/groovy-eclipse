/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy23;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This is a 'script' to compute an updated list of exported packages for this bundle.
 * Run this simply as a Java application. It prints a list of all packages in the groovy-all jar.
 * Merged with any exported packages from the current MANIFEST.MF.
 * <p>
 * Paste this updated list into MANIFEST.MF to replace the old one. This ensures that there are
 * no osgi-hidden 'private' packages in groovy-all jar. These will cause problems with some
 * AST transforms accidentally picking up classes from the project's classpath instead of our
 * groovy-all jar.
 *
 * Note: this script is in a source folder compiled by eclipse, but isn't in build.properties. So
 * it won't be built by maven for CI and RELEASE builds. It is *not* part of Greclipse.
 *
 * @author Kris De Volder
 */
public class UpdateExportedPackages {

    public static final String GROOVY_ALL_PATH = "lib/groovy-all-2.4.12.jar";
    public static final String MANIFEST = "META-INF/MANIFEST.MF";

    public static void main(String[] args) throws Exception {
        JarFile jar = new JarFile(new File(GROOVY_ALL_PATH));
        try {
            TreeSet<String> packages = new TreeSet<String>();
            readCurrentManifest(packages);

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry el = entries.nextElement();
                String pathName = el.getName();
                if (pathName.endsWith(".class")) {
                    int lastSlash = pathName.lastIndexOf('/');
                    if (lastSlash>=0) {
                        String pkg = pathName.substring(0, lastSlash).replace('/', '.');
                        packages.add(pkg);
                    }
                }
            }

            boolean first = true;
            for (String pkg : packages) {
                System.out.print(first?"Export-Package: ":",\n ");
                System.out.print(pkg);
                first = false;
            }
            System.out.println();
        } finally {
            jar.close();
        }
    }

    private static void readCurrentManifest(TreeSet<String> packages) throws Exception {
        Manifest manifest = new Manifest();
        InputStream is = new FileInputStream(new File(MANIFEST));
        try {
            manifest.read(is);
            String epa = manifest.getMainAttributes().getValue("Export-Package");
            String[] pkgs = epa.split("\\,(\\s)*");
            for (String p : pkgs) {
                packages.add(p);
            }
        } finally {
            is.close();
        }
    }
}
