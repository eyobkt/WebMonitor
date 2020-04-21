package eyobkt.restmonitor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MonitorServiceCreator implements ServletContextListener {
  
  public void contextInitialized(ServletContextEvent sce) {         
    ServletContext sContext = sce.getServletContext();
    sContext.addServlet("monitorService", new MonitorService(new MonitorDaoFactory()))
        .addMapping("/api/v1/monitor");
  }
  
  public void contextDestroyed(ServletContextEvent sce) {}
}