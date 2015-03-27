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
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * GrapeEngine that uses Maven API directly to resolve dependencies.
 *
 * @author yihtserns
 */
public class GrapeMaven implements GrapeEngine {

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
        URLClassLoader classLoader = (URLClassLoader) args.get("classLoader");
        if (classLoader == null) {
            return null;
        }
        File[] files = Maven.resolver().resolve(asArray(dependencies)).withTransitivity().asFile();

        for (File file : files) {
            classLoader.addURL(file.toURI().toURL())
        }

        return null;
    }

    private String[] asArray(Map<String, String>[] deps) {
        List<String> x = new ArrayList<String>();
        for (Map<String, String> dep : deps) {
            String group = dep.get("group");
            String module = dep.get("module");
            String version = dep.get("version");
            x.add(group + ":" + module + ":" + version);
        }

        return x.toArray(new String[x.size()]);
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
}
