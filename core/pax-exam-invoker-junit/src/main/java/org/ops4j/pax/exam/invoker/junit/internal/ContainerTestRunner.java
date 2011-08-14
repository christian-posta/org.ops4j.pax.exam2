/*
 * Copyright 2011 Harald Wellmann
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
package org.ops4j.pax.exam.invoker.junit.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerTestRunner extends BlockJUnit4ClassRunner
{
    private static Logger LOG = LoggerFactory.getLogger( ContainerTestRunner.class );

    private BundleContext m_ctx;
    private Injector m_injector;
    
    public ContainerTestRunner( Class<?> klass, BundleContext context, Injector injector ) throws InitializationError
    {
        super( klass );
        m_ctx = context;
        m_injector = injector;        
    }

    @Override
    protected Object createTest() throws Exception
    {
        Object test = super.createTest();
        m_injector.injectFields( m_ctx, test );
        return test;
    }
    
    @Override
    protected void runChild( FrameworkMethod method, RunNotifier notifier )
    {
        LOG.info("running {} in reactor", method);
        super.runChild( method, notifier );
    }
    
    @Override
    protected void validatePublicVoidNoArgMethods( Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors )
    {

    }

    public void setContext(BundleContext context) {
        m_ctx = context;
    }
    
    public void setInjector(Injector injector) {
        m_injector = injector;
    }
    
}
