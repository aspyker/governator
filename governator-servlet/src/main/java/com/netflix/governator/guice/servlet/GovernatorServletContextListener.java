package com.netflix.governator.guice.servlet;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.LifecycleShutdownSignal;

/**
 * An extension of {@link GuiceServletContextListener} which integrates with Governator's
 * LifecycleInjector.  This implementation drives shutdown of LifecycleManager through the 
 * ServletContextListener's contextDestroyed event.  
 * 
 * To use, subclass your main server class from GovernatorServletContextListener
 * <pre>
 * {@code 
 * 
package com.cloudservice.StartServer;
public class StartServer extends GovernatorServletContextListener
{
    @Override
    protected Injector createInjector() {
        return Governator.createInjector(
            new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    serve("/REST/*").with(GuiceContainer.class);
                    binder().bind(GuiceContainer.class).asEagerSingleton();
                    
                    bind(MyResource.class).asEagerSingleton();
                }
            }
        );
    }
}
 * }
 * </pre>
 * 
 * Then reference this class from web.xml.
 *
 <PRE>
     &lt;filter&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;filter-class&gt;com.google.inject.servlet.GuiceFilter&lt;/filter-class&gt;
     &lt;/filter&gt;

     &lt;filter-mapping&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
     &lt;/filter-mapping&gt;

     &lt;listener&gt;
         &lt;listener-class&gt;com.cloudservice.StartServer&lt;/listener-class&gt;
     &lt;/listener&gt;

 </PRE>
 *
 * @author Eran Landau
 */
public abstract class GovernatorServletContextListener extends GuiceServletContextListener {
    protected static final Logger LOG = LoggerFactory.getLogger(GovernatorServletContextListener.class);

    static final String INJECTOR_NAME = Injector.class.getName();

    private Injector injector;
    
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        injector.getInstance(LifecycleShutdownSignal.class).signal();
    }

    /**
     * Override this method to create (or otherwise obtain a reference to) your
     * injector.
     */
    protected final Injector getInjector() {
        injector = createInjector();
        return injector;
    }
    
    protected abstract Injector createInjector();

}
