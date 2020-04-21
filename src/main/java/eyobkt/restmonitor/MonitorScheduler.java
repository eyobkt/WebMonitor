package eyobkt.restmonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MonitorScheduler implements ServletContextListener {
  
  ScheduledExecutorService seService;
  
  public void contextInitialized(ServletContextEvent sce) {   
    MonitorDaoFactory mdf = new MonitorDaoFactory();    
    seService = Executors.newSingleThreadScheduledExecutor();
    seService.scheduleAtFixedRate(new MonitoringRound(mdf), 0, 60, TimeUnit.MINUTES);
  }
  
  public void contextDestroyed(ServletContextEvent sce) {
    seService.shutdownNow();
  }
}