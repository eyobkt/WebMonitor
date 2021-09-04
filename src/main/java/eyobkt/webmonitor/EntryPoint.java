package eyobkt.webmonitor;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import eyobkt.webmonitor.config.DispatcherServletContextConfiguration;
import eyobkt.webmonitor.config.RootContextConfiguration;

public class EntryPoint implements WebApplicationInitializer {  
  
  @Override
  public void onStartup(ServletContext container) {
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.register(RootContextConfiguration.class);    
    container.addListener(new ContextLoaderListener(rootContext));
    
    AnnotationConfigWebApplicationContext dispatcherServletContext = new AnnotationConfigWebApplicationContext(); 
    dispatcherServletContext.register(DispatcherServletContextConfiguration.class);
    ServletRegistration.Dynamic dispatcherServletRegistration = container.addServlet("dispatcherServlet", 
        new DispatcherServlet(dispatcherServletContext));
    dispatcherServletRegistration.setLoadOnStartup(1);
    dispatcherServletRegistration.addMapping("/");
  }  
}
