package eyobkt.restapichangemonitor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MonitorScheduler implements ServletContextListener {
  
  ScheduledExecutorService seService;
  
  public void contextInitialized(ServletContextEvent sce) {    
    seService = Executors.newSingleThreadScheduledExecutor();
    seService.scheduleAtFixedRate(new MonitoringRound(new MonitorDaoFactory()), 0, 1, TimeUnit.MINUTES);
  }
  
  public void contextDestroyed(ServletContextEvent sce) {
    seService.shutdownNow();
  }
}