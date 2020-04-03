import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MonitorScheduler implements ServletContextListener {
  ScheduledExecutorService ses;
  
  public void contextInitialized(ServletContextEvent sce) {
    ses = Executors.newSingleThreadScheduledExecutor();
    ses.scheduleAtFixedRate(new MonitoringRound(), 0, 1, TimeUnit.MINUTES);
  }
  
  public void contextDestroyed(ServletContextEvent sce) {
    ses.shutdownNow();
  }
}