package eyobkt.restmonitor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class EntryPoint implements ServletContextListener {
  
  MonitorScheduler monitorScheduler;
  
  public void contextInitialized(ServletContextEvent sce) {
    MonitorDaoFactory mdf = new MonitorDaoFactory(); 
    
    ServletContext sContext = sce.getServletContext();
    sContext.addServlet("monitorService", new MonitorService(mdf))
        .addMapping("/api/v1/monitor");
    
    monitorScheduler = new MonitorScheduler(mdf);    
  }
  
  public void contextDestroyed(ServletContextEvent sce) {
    monitorScheduler.shutdownNow();
  }
}