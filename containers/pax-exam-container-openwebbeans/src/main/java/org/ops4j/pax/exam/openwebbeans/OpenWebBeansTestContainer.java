/*
 * Copyright 2012 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.openwebbeans;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;

import org.apache.webbeans.cditest.CdiTestContainer;
import org.apache.webbeans.cditest.CdiTestContainerLoader;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.JarProbeOption;
import org.ops4j.pax.exam.spi.war.JarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class OpenWebBeansTestContainer implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebBeansTestContainer.class);

    private static CdiTestContainer container;

    private boolean isValid;

    private ExamSystem system;

    private ClassLoader contextClassLoader;

    private File probeDir;

    public OpenWebBeansTestContainer(ExamSystem system) {
        this.system = system;
    }

    public void call(TestAddress address) {
    }

    public long install(String location, InputStream stream) {
        return -1;
    }

    public long install(InputStream stream) {
        return -1;
    }

    public TestContainer start() {
        validateConfiguration();
        setProbeClassLoader();
        LOG.debug("starting OpenWebBeans container");
        container = CdiTestContainerLoader.getCdiContainer();
        try {
            container.bootContainer();
            container.startContexts();
            isValid = true;
        }
        // CHECKSTYLE:SKIP : OpenWebBeans API
        catch (Exception exc) {
            throw new TestContainerException(exc);
        }
        return this;
    }
    
    private void setProbeClassLoader() {
        JarProbeOption probeOption = system.getSingleOption(JarProbeOption.class);
        if (probeOption == null) {
            return;
        }
        
        probeDir = new File(system.getTempFolder(), UUID.randomUUID().toString());
        probeDir.mkdir();
        JarBuilder builder = new JarBuilder(probeDir, probeOption);
        URI jar = builder.buildJar();
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jar.toURL()});
            contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        catch (MalformedURLException exc) {
            throw new TestContainerException(exc);
        }
    }

    private void validateConfiguration() {
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty(Constants.EXAM_SYSTEM_KEY);
        if (! Constants.EXAM_SYSTEM_CDI.equals(systemType)) {
            String msg = "OpenWebBeansTestContainer requires pax.exam.system = cdi";
            throw new TestContainerException(msg);
        }
    }

    

    public TestContainer stop() {
        if (container != null && isValid) {
            LOG.debug("stopping OpenWebBeans container");
            try {
                container.stopContexts();
                container.shutdownContainer();
                unsetProbeClassLoader();
            }
            // CHECKSTYLE:SKIP : OpenWebBeans API
            catch (Exception exc) {
                throw new TestContainerException(exc);
            }
        }
        return this;
    }

    private void unsetProbeClassLoader() {
        if (contextClassLoader != null) {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public String toString() {
        return "OpenWebBeans";
    }

    public static CdiTestContainer getCdiContainer() {
        return container;
    }
    

    @Override
    public long installProbe(InputStream stream) {
        return -1;
    }

    @Override
    public void uninstallProbe() {
        // not used
    }    
}
