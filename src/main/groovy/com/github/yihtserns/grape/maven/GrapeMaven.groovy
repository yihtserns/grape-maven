/*
 * Copyright 2015 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.grape.maven;

import groovy.grape.GrapeEngine;
import java.io.File;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * GrapeEngine that uses Maven API directly to resolve dependencies.
 *
 * @author yihtserns
 */
public class GrapeMaven implements GrapeEngine {

    private static String[] LOGGER_NAMES = [
        "org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener",
        "org.jboss.shrinkwrap.resolver.impl.maven.logging.LogRepositoryListener",
        "org.jboss.shrinkwrap.resolver.impl.maven.logging.LogModelProblemCollector"
    ]

    public GrapeMaven() {
        def reportDownloads = Boolean.getBoolean("groovy.grape.report.downloads")
        if (reportDownloads) {
            def consoleHandler = new ConsoleHandler()
            consoleHandler.level = Level.FINEST
            consoleHandler.formatter = { logRecord -> logRecord.message + System.lineSeparator() }

            LOGGER_NAMES.each { name ->
                def logger = Logger.getLogger(name)
                logger.with {
                    useParentHandlers = false
                    level = Level.FINEST
                    addHandler(consoleHandler)
                }
            }
        }
    }

    @Override
    public Object grab(String endorsedModule) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object grab(Map args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object grab(Map args, Map... dependencies) {
        URLClassLoader loader = chooseClassLoader(
            classLoader: args.remove('classLoader'),
            refObject: args.remove('refObject'),
            calleeDepth: args.calleeDepth ?: 3,
        )

        if (loader == null) {
            return null;
        }

        File[] files = Maven.resolver()
        .resolve(toCanonicalForms(dependencies))
        .withTransitivity()
        .asFile();

        // 1. Register **all** dependencies in class loader first...
        for (File file : files) {
            loader.addURL(file.toURI().toURL())
        }

        // 2. ...before trying to load any classes here
        for (File file : files) {
            // Somehow, I expected these things to be implemented **outside** GrapeIvy
            processCategoryMethods(loader, file)
            processOtherServices(loader, file)
        }

        return null;
    }

    private List<String> toCanonicalForms(Map<String, String>[] deps) {
        List<String> canonicalForms = new ArrayList<String>();
        for (Map<String, String> dep : deps) {
            String group = dep.get("group");
            String module = dep.get("module");
            String version = dep.get("version");

            canonicalForms.add(group + ":" + module + ":" + version);
        }

        return canonicalForms;
    }

    /**
     * Copied from Groovy's GrapeIvy.
     */
    // Somehow this has to be public, else the resolved jars will be corrupted :/
    public ClassLoader chooseClassLoader(Map args) {
        def loader = args.classLoader
        if (!isValidTargetClassLoader(loader)) {
            loader = (args.refObject?.class ?:ReflectionUtils.getCallingClass(args.calleeDepth?:1))?.classLoader

            while (loader && !isValidTargetClassLoader(loader)) {
                loader = loader.parent
            }

            if (!isValidTargetClassLoader(loader)) {
                throw new RuntimeException("No suitable ClassLoader found for grab")
            }
        }
        return loader
    }

    /**
     * Copied from Groovy's GrapeIvy.
     */
    private boolean isValidTargetClassLoader(loader) {
        return isValidTargetClassLoaderClass(loader?.class)
    }

    /**
     * Copied from Groovy's GrapeIvy.
     */
    private boolean isValidTargetClassLoaderClass(Class loaderClass) {
        return (loaderClass != null) &&
        (
            (loaderClass.name == 'groovy.lang.GroovyClassLoader') ||
            (loaderClass.name == 'org.codehaus.groovy.tools.RootLoader') ||
            isValidTargetClassLoaderClass(loaderClass.superclass)
        )
    }

    /**
     * Copied from Groovy's GrapeIvy.
     */
    private processCategoryMethods(ClassLoader loader, File file) {
        if (!file.name.toLowerCase().endsWith(".jar")) {
            return
        }
        def mcRegistry = GroovySystem.metaClassRegistry
        if (!(mcRegistry instanceof MetaClassRegistryImpl)) {
            return
        }
        try {
            JarFile jar = new JarFile(file)
            def entry = jar.getEntry(MetaClassRegistryImpl.MODULE_META_INF_FILE)
            if (!entry) {
                return
            }
            Properties props = new Properties()
            props.load(jar.getInputStream(entry))

            Map<CachedClass, List<MetaMethod>> metaMethods = new HashMap<CachedClass, List<MetaMethod>>()
            mcRegistry.registerExtensionModuleFromProperties(props, loader, metaMethods)

            // add old methods to the map
            metaMethods.each { CachedClass c, List<MetaMethod> methods ->
                // GROOVY-5543: if a module was loaded using grab, there are chances that subclasses
                // have their own ClassInfo, and we must change them as well!
                Set<CachedClass> classesToBeUpdated = ClassInfo.allClassInfo.findAll {
                    CachedClass current = it.cachedClass
                    while (current != null) {
                        if (current == c || current.interfaces.contains(c)) {
                            return true
                        }
                        current = current.cachedSuperClass
                    }
                    return false
                }.collect { it.cachedClass }

                classesToBeUpdated*.addNewMopMethods(methods)
            }
        } catch(ZipException zipException) {
            throw new RuntimeException("Grape could not load jar '$file'", zipException)
        }
    }

    /**
     * Copied from Groovy's GrapeIvy.
     */
    private void processOtherServices(ClassLoader loader, File file) {
        try {
            ZipFile zip = new ZipFile(file)

            ZipEntry serializedCategoryMethods = zip.getEntry("META-INF/services/org.codehaus.groovy.runtime.SerializedCategoryMethods")
            if (serializedCategoryMethods != null) {
                zip.getInputStream(serializedCategoryMethods).text.readLines().each {
                    println it.trim() // TODO implement this or delete it
                }
            }

            ZipEntry pluginRunners = zip.getEntry("META-INF/services/org.codehaus.groovy.plugins.Runners")
            if (pluginRunners != null) {
                zip.getInputStream(pluginRunners).text.readLines().each {
                    GroovySystem.RUNNER_REGISTRY[file.getName()] = loader.loadClass(it.trim()).newInstance()
                }
            }
        } catch(ZipException ignore) {
            // ignore files we can't process, e.g. non-jar/zip artifacts
            // TODO log a warning
        }
    }


    @Override
    public Map<String, Map<String, List<String>>> enumerateGrapes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URI[] resolve(Map args, Map... dependencies) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URI[] resolve(Map args, List depsInfo, Map... dependencies) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map[] listDependencies(ClassLoader classLoader) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addResolver(Map<String, Object> args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Hack to allow Groovy Console to start up.
     *
     * @see https://github.com/groovy/groovy-core/blob/GROOVY_2_3_2/subprojects/groovy-console/src/main/groovy/groovy/ui/ConsoleIvyPlugin.groovy#L41
     */
    def getIvyInstance() {
        return [eventManager:[addIvyListener:{}]]
    }
}
